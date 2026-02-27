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
from prompts import STUDY_NOTES_AGENT_SYSTEM_PROMPT as SYSTEM_PROMPT
# from prompts import RANDOM_RESEARCHER_SYSTEM_PROMPT as SYSTEM_PROMPT

model = ChatOpenAI(
    # model="nvidia/nemotron-3-nano-30b-a3b:free",  # or any OpenRouter-supported model
	model="stepfun/step-3.5-flash:free",
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
	
	topic = "Spring Security"
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