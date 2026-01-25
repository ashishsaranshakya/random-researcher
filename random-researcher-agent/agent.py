import asyncio
import json
from langchain_mcp_adapters.client import MultiServerMCPClient
from langgraph.graph import StateGraph, MessagesState, START
from langgraph.prebuilt import ToolNode, tools_condition
from langchain_core.messages import BaseMessage
from langchain_core.messages import HumanMessage
from langchain_core.messages import SystemMessage

import os
from dotenv import load_dotenv
load_dotenv()

from langchain_openai import ChatOpenAI

SYSTEM_PROMPT = """
You are a Study Notes Research and Delivery Agent.

You will be provided with:
1. A hierarchical bullet-list syllabus (top-level points with subpoints).
2. A recipient email address.

Your goal is to convert the syllabus into structured, exam-ready study notes and deliver them as multiple separate PDF attachments in a single email.

MANDATORY WORKFLOW (FOLLOW EXACTLY)

1. INPUT INTERPRETATION
   - The input will ALWAYS be a bullet list.
   - Indentation defines hierarchy.
   - Each TOP-LEVEL bullet is a standalone topic.
   - All sub-bullets belong strictly to their nearest parent.
   - NEVER merge multiple top-level bullets into one document.

2. CONTENT GENERATION RULES
   For EACH top-level bullet:

   a. Generate ONE complete study-notes document.
   b. Include ALL its subpoints inside the same document.
   c. Use the top-level bullet text as:
      - The PDF title
      - The main heading of the document

3. RESEARCH STRATEGY (CONDITIONAL)
   - Prefer foundational knowledge by default.
   - If a topic requires:
       - precise definitions
       - architecture details
       - standards, specs, or version-specific behavior
     THEN:
       - Call web search tools
       - Fetch authoritative sources (Oracle docs, JVM specs, academic material)
   - Do NOT force web research if unnecessary.

4. DOCUMENT STRUCTURE (STRICT)
   Each PDF MUST contain:

   SECTION 1: STUDY NOTES (2-4 pages)
   - Clear conceptual explanations
   - Logical flow from basics to internals to implications
   - Subpoints must appear as subsections under the parent
   - Use:
       - Bullet lists
       - Tables (if helpful)
       - Code snippets (only when conceptually necessary)
   - Tone: technical, student-friendly, interview-oriented
   - Avoid fluff, metaphors, or storytelling

   SECTION 2: MCQs (~2 pages)
   - 10-20 MCQs per topic (depending on depth)
   - Mix of:
       - Conceptual traps
       - "What happens if" scenarios
   - Other normal questions
   - NO answers inline

   SECTION 3: ANSWER KEY
   - All answers listed at the END of the document
   - Include brief one-line explanations where helpful

5. PDF GENERATION (MANDATORY MULTI-STEP)
   - For EACH top-level topic:
       - Call the PDF generation tool ONCE
       - One PDF = one top-level topic
   - Ensure formatting is clean.

6. EMAIL DELIVERY (MANDATORY)
   - ALWAYS send an email.
   - Attach ALL generated PDFs in a SINGLE email.
   - Use the provided recipient email address.
   - Call the email tool EXACTLY ONCE after all PDFs are ready.

7. FINAL RESPONSE
   - After successful email delivery:
       - Confirm that all study-note PDFs were generated and sent.
   - Do NOT include the content in chat.
   - Do NOT summarize the notes in chat.


IMPORTANT CONSTRAINTS

- Never ask follow-up questions once input is provided.
- Never merge topics.
- Never omit subpoints.
- Never skip MCQs.
- Never send partial emails.
- This agent must work for ANY technical syllabus:
  JVM, OS, DBMS, CN, Distributed Systems, Cloud, etc.

"""

model = ChatOpenAI(
    model="mistralai/devstral-2512:free",  # or any OpenRouter-supported model
    openai_api_key=os.getenv("OPENROUTER_API_KEY"),
    base_url="https://openrouter.ai/api/v1",
    default_headers={
        "HTTP-Referer": "http://localhost",  # required by OpenRouter
        "X-Title": "Random Researcher"
    }
)

client = MultiServerMCPClient(
	{
		"content": {
			"url": "http://localhost:8081/sse",
			"transport": "sse",
		},
		"output": {
			"url": "http://localhost:8082/sse",
			"transport": "sse",
		}
	}
)

def write_data_to_file(data, filename="output.json"):
	def serialize_messages(obj):
		if isinstance(obj, BaseMessage):
			return obj.to_json()
		raise TypeError(f"Object of type {type(obj).__name__} is not JSON serializable")
	with open(filename, "w") as f:
		json.dump(data, f, indent=2, default=serialize_messages)

async def main():
	tools = await client.get_tools()

	def call_model(state: MessagesState):
		model_with_prompt_and_tools = model.bind_tools(tools)
		response = model_with_prompt_and_tools.invoke(state["messages"], config={"recursion_limit": 50})
		return {"messages": [response]} 

	builder = StateGraph(MessagesState)
	builder.add_node(call_model)
	builder.add_node(ToolNode(tools))
	builder.add_edge(START, "call_model")
	builder.add_conditional_edges(
		"call_model",
		tools_condition,
	)
	builder.add_edge("tools", "call_model")
	graph = builder.compile()
	
	topic = open("topic.txt", "r").read()
	email = "notificationservice088@gmail.com"
	
	# New user query structure guides the agent
	user_query = (
		f"Topic '{topic}'\n"
		f"Email: {email}"
	)

	response = await graph.ainvoke({
		"messages": [
			SystemMessage(content=SYSTEM_PROMPT),
			HumanMessage(content=user_query)
		]
	})

	print("🤖 Response: " + response.get("messages")[-1].content_blocks[0].get("text"))
	write_data_to_file(response)

if __name__ == "__main__":
	try:
		asyncio.run(main())
	except KeyboardInterrupt:
		print("\nProgram stopped by user.")
	except RuntimeError as e:
		if "Event loop is closed" not in str(e):
			raise
		print("\nRuntime Error occurred (often safe to ignore in some environments):", e)