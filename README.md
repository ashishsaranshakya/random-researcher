# Agentic Research Synthesizer

A hobby AI systems project exploring ReAct-style agents, tool orchestration, and microservice-based LLM architectures.

This system dynamically researches a topic, plans what materials to generate, creates PDFs, and emails them to the user.

---

## Project Structure

```
./content-mcp-server
./output-mcp-server
./random-researcher-agent
./random-researcher-agent-spring
```

---

## Architecture

User → Agent → MCP Tool Servers → LLM (OpenRouter) → PDF + Email

- Agents orchestrate reasoning and tool usage.
- MCP servers expose research and output tools.
- Both Python and Java agents use the same tool servers.

---

## Components

### 1. `random-researcher-agent` (Python, CLI)

- Built with **LangGraph**
- Uses **OpenRouter**
- Runs via CLI (no server)
- Implements full ReAct loop

Modes:
- **Random Researcher** – broad exploratory research
- **Study Agent** – structured study material generation

---

### 2. `random-researcher-agent-spring` (Java, REST API)

- Built with **Spring Boot + Spring AI + LangGraph4j**
- Uses **OpenRouter**
- Synchronous (blocking)
- Exposes REST endpoints

Endpoints:

```
POST /agent/run-random-researcher
POST /agent/run-study-agent
```

Example request:

```json
{
  "topic": "P8I Poseidon Patrol Aircraft",
  "email": "example@email.com"
}
```

---

### 3. `content-mcp-server` (Java)

Research tools:
- `google_search_tool`
- `fetch_content`

---

### 4. `output-mcp-server` (Java)

Output tools:
- `generate_pdf`
- `send_email`

---

## Key Concepts Explored

- ReAct (Reason + Act) architecture
- Tool calling via MCP
- Multi-service AI systems
- LangGraph (Python) vs LangGraph4j (Java)
- Prompt-driven behavioral modes

