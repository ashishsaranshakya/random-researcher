# Agentic Research Synthesizer

An autonomous, agentic AI system that dynamically researches, plans, synthesizes, and delivers customized PDF materials for any given topic.

## Overview

This project implements an **agentic AI system** that functions as an autonomous research assistant. Given a user-provided topic, the system doesn't just execute a fixed, hard-coded pipeline. Instead, it embodies a core agentic characteristic: **dynamic planning**.

Here is the process:

1.  The agent first performs initial research to understand the topic's breadth and depth.
2.  It then uses an LLM (Google Gemini) to **reason** about the gathered information.
3.  Based on this reasoning, it dynamically decides which study materials (e.g., detailed notes, quick facts, Q&A) are most appropriate and **feasible** to generate.
4.  It executes this custom plan, using its tools to generate the content and convert it to PDF.
5.  As the final step, it uses an email tool to deliver the completed set of PDFs directly to the user.

This approach ensures the output is not only tailored to the topic but also conveniently delivered.

---

## Features

* **Agentic Core:** Built using a **ReAct (Reason + Act)** architecture. The agent thinks, plans, and then executes its plan by using its available tools.
* **Dynamic Planning:** The agent's primary strength. It intelligently decides *what* to create, resulting in a more relevant and high-quality learning packet.
* **Microservice Architecture:** The system is decoupled. A central Python agent orchestrates multiple Java-based microservices (MCP servers), which provide the tools that the agent can use.
* **Custom Content Generation:** Autonomously generates a variety of study materials, such as notes, facts, and Q&A sheets.
* **PDF Output:** All generated materials are compiled into clean, easy-to-read PDF documents.
* **Email Delivery:** The agent's final action is to automatically email the generated PDFs to a specified user.

---

## Components

### 1. `random-researcher-agent` (Python / LangGraph)
* **Researcher Agent:** This is the core agent that orchestrates the entire process.
* It implements the **ReAct** logic using langgraph and Google Gemini.
* It manages the workflow, from receiving the initial topic to calling its tools and delivering the final files.
* It uses the tools provided by the MCP servers to be called when its plan requires.

### 2. `content-mcp-server` (Java / Spring Boot)
* **Content Tool Server:** This is a **Model Content Protocol (MCP)** server for content related tools.
* `google_search_tool` tool: Searches the web for general knowledge and returns top 10 snippets.
* `fetch_content` tool: Fetches and cleans the main text from a given URL for LLM processing.

### 3. `output-mcp-server` (Java / Spring Boot)
* **Output Tool Server:** This is a **Model Content Protocol (MCP)** server for output realted tools.
* `generate_pdf` tool: Converts final markdown study content to a PDF file and returns the local file path.
* `send_email` tool: Send the generated PDF files as an attachments to the specified recipient.

---

## Technical Stack

* **Agent Logic:** Python, LangGraph and Google Gemini
* **Tools / Microservices:** Java, Spring Boot
* **Build:** Maven (for Java), pip (for Python)
* **Protocol:** Model Content Protocol (MCP)
