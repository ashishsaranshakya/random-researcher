Start-Process powershell `
  -ArgumentList "-NoExit", "-Command",
  "cd '$PSScriptRoot'; java -jar .\content-mcp-server\target\content-mcp-server-0.0.1-SNAPSHOT.jar"

Start-Process powershell `
  -ArgumentList "-NoExit", "-Command",
  "cd '$PSScriptRoot'; java -jar .\output-mcp-server\target\output-mcp-server-0.0.1-SNAPSHOT.jar"
