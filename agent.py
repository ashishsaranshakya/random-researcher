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

from langchain.chat_models import init_chat_model

SYSTEM_PROMPT = (
    "You are a Random topic Research and Delivery Agent"
	"You will be provided with a research topic and a recipient email address."
    "Your primary goal is to process a single topic, create multiple structured documents, "
    "and deliver all documents as multiple seperate PDF attachments in a single email."
    "The recipient email address will be included in the user's initial query."
    "Your workflow MUST follow this mandatory sequence precisely:\n\n"
    "1. **Deep Research:** "
    "   a. Call the `google_search_tool` with the user's query.\n"
    "   b. Analyze the search results and identify the most relevant, unique URLs.\n"
    "   c. Call the `fetch_content` tool for EACH of the 5 identified URLs to gather comprehensive data.\n"
    "2. **Synthesis & Structuring:** Based on the research, generate the following content blocks. The 'Research Summary' is mandatory. The 'Facts', 'QnA', and 'Quiz' sections are conditional—generate them only if the topic is suitable (e.g., military, historical or scientific topics usually warrant some of them):\n"
    "   - **Research Summary (Mandatory):** A comprehensive 2 page summary of the topic.\n"
    "   - **Facts:** A list of key, interesting facts.(stats, metrics, etc.)\n"
    "   - **QnA:** A list of 5-10 common questions and their answers.\n"
    "   - **Quiz:** A short quiz with 3-5 questions and answers.\n"
    "3. **PDF Conversion (Multi-Step Mandatory):** For *every* block of content you generate (Summary, Facts, QnA, Quiz), you MUST call the `generate_pdf` tool separately. Each call should use the corresponding markdown content block (e.g., 'content' argument for the first call is the summary text, 'content' for the second call is the facts text, and so on).\n"
    "4. **Collect File Paths:** Collect the file path returned by each successful `generate_pdf` call.\n"
    "5. **Email Delivery (Final Mandatory Step):** After all required PDFs have been successfully generated and their file paths collected, you MUST call the `send_study_guide_email` tool EXACTLY ONCE. Pass ALL collected PDF file paths in a list to the 'file_paths' argument, along with the recipient email address.\n"
    "6. **Final Acknowledgment:** After successful emailing, acknowledge that the full set of research reports has been processed and sent."
)

model = init_chat_model(
	model_provider = "google_genai",
	model = "gemini-2.5-flash",
	google_api_key = os.getenv("GEMINI_API_KEY")
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
	
	topic = "F1 2025 season"
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