import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent
from tool import tools_list

load_dotenv()


def get_agent_executor(tool_list=tools_list):
   llm = ChatOpenAI(
      base_url="http://localhost:11434/v1",
      api_key="ollama",
      model="llama3.1",
      temperature=0.5,
   )

   system_prompt = (
      "You are a professional multi-tasking agent. "
      "Your goal is to solve the user's request using the tools provided. "
      "Logic Rules:\n"
      "1. Analyze the user query to see which tools are needed.\n"
      "2. If a task has multiple steps, perform them one by one.\n"
      "3. Use the output of one tool to inform the next tool call.\n"
      "4. If you cannot find information, say so clearly.\n"
      "5. Always be concise and factual."
   )

   agent_executor = create_react_agent(
      model=llm,
      tools=tool_list,
      prompt=system_prompt,
   )
   return agent_executor