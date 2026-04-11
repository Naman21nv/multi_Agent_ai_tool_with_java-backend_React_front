from dotenv import load_dotenv
# Load environment variables before importing other modules that might rely on them.
load_dotenv()

from tool import tools_list
from agent_engine import get_agent_executor


def start_chat():
    # 1. INITIALIZATION FLOW: 
    # Get the configured ReAct agent executor. This executor encapsulates the LangGraph 
    # state machine that inherently handles the "Think -> Act -> Observe" loop.
    agent_executor = get_agent_executor(tool_list=tools_list)


    while True:
        # 2. USER INPUT FLOW: Collect the prompt from the user.
        user_input = input("Enter your request (or 'x' to quit): ")
        if user_input.lower() == "x":
            break
        
        # 3. AGENT EXECUTION FLOW (The Core Concept): 
        # We pass the user's input into the agent's state under the "messages" key.
        # The agent executor takes this, passes it to the LLM, and starts the reasoning loop.
        # - THINK: LLM decides what to do.
        # - ACT: If it needs a tool, the executor intercepts the request and runs the Python function.
        # - OBSERVE: The tool's output is appended to the messages, and the LLM is prompted again.
        response = agent_executor.invoke({"messages": [{"role": "user", "content": user_input}]})
        
        # 4. PARSING THE TRACE & OUTPUT:
        # LangGraph returns the entire state history. We iterate through the messages
        # to give the user visibility into what the agent was thinking/doing behind the scenes.
        if "messages" in response and response["messages"]:
            print("\n--- Agent Trace ---")
            # Skip the first message (the user's prompt) to only show agent actions.
            for msg in response["messages"][1:]:
                # If it's an AI message AND has tool_calls, the agent decided to "Act".
                if msg.type == "ai" and getattr(msg, "tool_calls", None):
                    print(f"🛠️  Calling Tool: {[t['name'] for t in msg.tool_calls]}")
                # If it's a Tool message, the tool finished executing (the "Observe" phase).
                elif msg.type == "tool":
                    print(f"✅  Tool '{msg.name}' Finished.")
            print("-------------------\n")
            
            # THEORY: The ReAct loop concludes when the LLM decides it has enough information 
            # to answer the user directly without calling another tool. 
            # The last message in the list is always this final generated answer.
            output = response["messages"][-1].content
            
            # Fallback: Local, smaller models (like Llama 3.1 8B) can sometimes lose track 
            # of formatting instructions and return empty final responses after tool calls.
            if not output or not str(output).strip():
                output = "⚠️ The agent finished but returned an empty response. (Common with local models)"
        else:
            output = f"⚠️ Unexpected response: {response}"
            
        print(f"  Final Response: {output}\n")

if __name__ == "__main__":
    start_chat()
