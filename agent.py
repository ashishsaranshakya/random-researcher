import asyncio
import json
from langchain_mcp_adapters.client import MultiServerMCPClient
from langgraph.graph import StateGraph, MessagesState, START
from langgraph.prebuilt import ToolNode, tools_condition
from langchain_core.messages import BaseMessage

import os
from dotenv import load_dotenv
load_dotenv()

from langchain.chat_models import init_chat_model
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
		# "output": {
		# 	"url": "http://localhost:8082/sse",
        #     "transport": "sse",
		# }
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
		response = model.bind_tools(tools).invoke(state["messages"])
		return {"messages": response}

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
	math_response = await graph.ainvoke({"messages": "did india win the women's cricket world cup 2025?, use given search tool is provided"})
	print(math_response)
	print("____________________")
	write_data_to_file(math_response)
	# print(math_response["messages"].forEach(lambda m: print(m.content)))
	
	# weather_response = graph.ainvoke({"messages": "what is the weather in nyc?"})

if __name__ == "__main__":
    try:
        # This is the necessary fix: running the async main() function
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nProgram stopped by user.")
    except RuntimeError as e:
        # Catches common issues like "Event loop is closed" if run in environments like Jupyter
        if "Event loop is closed" not in str(e):
            raise
        print("\nRuntime Error occurred (often safe to ignore in some environments):", e)