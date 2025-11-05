import asyncio
import json
from langchain_mcp_adapters.client import MultiServerMCPClient
from langgraph.graph import StateGraph, MessagesState, START
from langgraph.prebuilt import ToolNode, tools_condition
from langchain_core.messages import BaseMessage
from langchain_core.messages import HumanMessage

import os
from dotenv import load_dotenv
load_dotenv()

from langchain.chat_models import init_chat_model

# System instruction is crucial for guiding the multi-step agent flow
S2YSTEM_PROMPT = (
	"You are a sophisticated Study Guide Agent. Your task is to process a user request "
	"that includes a 'topic' and a 'recipient email'.\n"
	"Your workflow MUST follow these steps precisely:\n"
	"1. **Content Generation:** Generate comprehensive study notes (2 pages mandatory), "
	"followed by optional sections for flash cards, Q&A, and a quiz, based on the requested topic. "
	"You may use google_search_tool or fetch_content to research, but then you MUST generate the full text content yourself.\n"
	"2. **PDF Conversion:** After generating ALL the content in a single large text block, "
	"you MUST call the `generate_pdf` tool. Pass the ENTIRE generated text content as the 'content' argument.\n"
	"3. **Email Delivery:** Once the `generate_pdf` tool returns the local file path of the PDF, "
	"you MUST immediately call the `send_study_guide_email` tool using the recipient email and the returned file path.\n"
	"4. **Final Response:** After successful emailing, inform the user that the study guide has been sent to their email."
)
S2YSTEM_PROMPT = (
    "You are a Random topic Research and Delivery Agent"
	"You will be provided with a research topic and a recipient email address."
    "Your primary goal is to process a single topic, create multiple structured documents, "
    "and deliver all documents as multiple seperate PDF attachments in a single email."
    "The recipient email address will be included in the user's initial query."
    "Your workflow MUST follow this mandatory sequence precisely:\n\n"
    "1. **Research:** Use the `google_search_tool` or `fetch_content` to gather background information on the user's topic.\n"
    "2. **Synthesis & Structuring:** Based on the research, generate the following content blocks. The 'Research Summary' is mandatory. The 'Facts', 'QnA', and 'Quiz' sections are conditional—generate them only if the topic is suitable (e.g., military topics, historical or scientific topics usually warrant some of them):\n"
    "   - **Research Summary (Mandatory):** A comprehensive 2 page summary of the topic.\n"
    "   - **Facts:** A list of key, interesting facts.(stats, metrics, etc.)\n"
    "   - **QnA:** A list of 5-10 common questions and their answers.\n"
    "   - **Quiz:** A short quiz with 3-5 questions and answers.\n"
    "3. **PDF Conversion (Multi-Step Mandatory):** For *every* block of content you generate (Summary, Facts, QnA, Quiz), you MUST call the `generate_pdf` tool separately. Each call should use the corresponding content block (e.g., 'content' argument for the first call is the summary text, 'content' for the second call is the facts text, and so on).\n"
    "4. **Collect File Paths:** Collect the file path returned by each successful `generate_pdf` call.\n"
    "5. **Email Delivery (Final Mandatory Step):** After all required PDFs have been successfully generated and their file paths collected, you MUST call the `send_study_guide_email` tool EXACTLY ONCE. Pass ALL collected PDF file paths in a list to the 'file_paths' argument, along with the recipient email address.\n"
    "6. **Final Acknowledgment:** After successful emailing, acknowledge that the full set of research reports has been processed and sent."
)
S2YSTEM_PROMPT = (
    "You are a Multi-Document Research and Delivery Agent deployed in a mandatory delivery pipeline. "
    "Your primary goal is to process a single research topic, create multiple structured documents, "
    "and deliver all documents as attachments in a single email."
    "The recipient email address will be included in the user's initial query."
    "Your workflow MUST follow this mandatory sequence precisely:\n\n"
    "1. **Deep Research:** "
    "   a. Call the `Google Search_tool` with the user's query.\n"
    "   b. Analyze the search results and identify the top 5 most relevant, unique URLs.\n"
    "   c. Call the `fetch_content` tool for EACH of the 5 identified URLs to gather comprehensive data.\n"
    "2. **Synthesis & Structuring:** Based on ALL gathered content, generate the following content blocks. The 'Research Summary' is mandatory. If the topic is military, defense, historical, or scientific, ALL other sections (Facts, QnA, Quiz) are MANDATORY to generate:\n"
    "   - **Research Summary (Mandatory):** A comprehensive summary of the topic.\n"
    "   - **Facts (Mandatory):** A list of at least 10 key, interesting facts.\n"
    "   - **QnA (Mandatory for Military/Science/History):** A list of 5-10 common questions and their answers.\n"
    "   - **Quiz (Mandatory for Military/Science/History):** A short quiz with 3-5 questions and answers.\n"
    "3. **PDF Conversion (Multi-Step Mandatory):** For *every* block of content you generate (Summary, Facts, QnA, Quiz), you MUST call the `generate_pdf` tool separately. Each call should use the corresponding content block.\n"
    "4. **Collect File Paths:** Collect the file path returned by each successful `generate_pdf` call.\n"
    "5. **Email Delivery (Final Mandatory Step):** After all required PDFs have been successfully generated and their file paths collected, you MUST call the `send_study_guide_email` tool EXACTLY ONCE. Pass ALL collected PDF file paths in a list to the 'file_paths' argument, along with the recipient email address.\n"
    "6. **Final Acknowledgment:** After successful emailing, acknowledge that the full set of research reports has been processed and sent."
)

SYSTEM_PROMPT = (
    "You are a Multi-Document Research and Delivery Agent deployed in a mandatory delivery pipeline. "
    "Your primary goal is to process a single research topic, create multiple structured documents, "
    "and deliver all documents as attachments in a single email."
    "The recipient email address will be included in the user's initial query."
    "Your workflow MUST follow this mandatory sequence precisely:\n\n"
    "1. **Deep Research (Mandatory 6 Tool Calls Minimum):** "
    "   a. Call the `Google Search_tool` with the user's query.\n"
    "   b. Analyze the search results and identify the top 5 most relevant, unique URLs.\n"
    "   c. Call the `fetch_content` tool for EACH of the 5 identified URLs. This step is MANDATORY and must be repeated 5 times to gather comprehensive data from 5 sources.\n"
    "2. **Synthesis & Structuring:** Based on ALL gathered content, generate the following content blocks. The 'Research Summary' and 'Facts' sections are mandatory for ALL topics. The 'QnA' and 'Quiz' sections are mandatory if the topic is military, defense, historical, or scientific:\n"
    "   - **Research Summary (Mandatory):** A comprehensive summary of the topic.\n"
    "   - **Facts (Mandatory for ALL Topics):** A list of at least 10 key, interesting facts.\n"
    "   - **QnA (Mandatory for Military/Science/History):** A list of 5-10 common questions and their answers.\n"
    "   - **Quiz (Mandatory for Military/Science/History):** A short quiz with 3-5 questions and answers.\n"
    "3. **PDF Conversion (Multi-Step Mandatory):** For *every* block of content you generate (Summary, Facts, QnA, Quiz), you MUST call the `generate_pdf` tool separately. Each call should use the corresponding content block.\n"
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
		model_with_prompt_and_tools = model.bind_tools(tools).bind(system_instruction=SYSTEM_PROMPT)
		response = model_with_prompt_and_tools.invoke(state["messages"])
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
	
	topic = "Akula class submarines"
	email = "notificationservice088@gmail.com"
	
	# New user query structure guides the agent
	user_query = (
		f"Topic '{topic}'\n"
		f"Email: {email}"
	)

	math_response = await graph.ainvoke({
		"messages": [HumanMessage(content=user_query)]
	})
	
	print(math_response)
	print("____________________")
	write_data_to_file(math_response)

if __name__ == "__main__":
	try:
		# asyncio.run(main())
		pass
	except KeyboardInterrupt:
		print("\nProgram stopped by user.")
	except RuntimeError as e:
		if "Event loop is closed" not in str(e):
			raise
		print("\nRuntime Error occurred (often safe to ignore in some environments):", e)