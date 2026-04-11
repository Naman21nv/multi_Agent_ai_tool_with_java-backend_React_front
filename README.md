# Local ReAct AI Agent

## Overview

This project provides a powerful and flexible framework for building and running a ReAct (Reasoning and Acting) AI agent on your local machine. The agent leverages the LangGraph library to execute complex tasks by reasoning about the problem and using a set of custom tools to find solutions.

It is designed for privacy, cost-effectiveness, and offline capability by running entirely locally using Ollama and an open-source Large Language Model (LLM) like Llama 3.1.

## How It Works (Project Flow)

The core of the project is a ReAct agent, which follows a "Think -> Act -> Observe" loop to solve problems.

1.  **User Input**: The agent receives a request from the user.
2.  **Think**: The LLM (e.g., Llama 3.1) analyzes the request, the conversation history, and the available tools. It then decides on a plan, which might involve using a tool.
3.  **Act**: If the LLM decides to use a tool, the agent executor calls the corresponding Python function (e.g., a web search tool, a calculator).
4.  **Observe**: The agent captures the output from the tool.
5.  **Repeat**: The output is fed back to the LLM. The LLM then re-evaluates the situation based on this new information and decides on the next step: either call another tool or generate the final answer for the user.

This entire process is managed by the `create_react_agent` function from LangGraph, which creates a robust, stateful agent executor.

## Key Technologies

-   **Python 3.9+**
-   **LangChain**: The core framework for building LLM applications.
-   **LangGraph**: An extension of LangChain for creating stateful, multi-step agent applications.
-   **Ollama**: For serving open-source LLMs locally.
-   **Dotenv**: For managing environment variables.

## Project Structure

```
.
├── .env                # Environment variables (e.g., API keys for tools)
├── agent_engine.py     # Core logic for creating the agent executor
├── tool.py             # Definition of custom tools for the agent
├── requirements.txt    # Python package dependencies
└── main.py             # Example script to run the agent
```

## Setup and Installation

### 1. Prerequisites

-   **Python 3.9 or higher**: Make sure you have Python installed.
-   **Ollama**: You must have Ollama installed and running. You can download it from https://ollama.com/.

### 2. Install the LLM

Once Ollama is running, pull the `llama3.1` model by running the following command in your terminal:

```bash
ollama pull llama3.1
```

### 3. Clone and Set Up the Project

```bash
# Clone the repository (if it's in a git repo)
# git clone <your-repo-url>
# cd <your-repo-name>

# Create a virtual environment
python -m venv venv
source venv/bin/activate  # On Windows use `venv\Scripts\activate`

# Install the required packages
pip install -r requirements.txt
```

## Configuration

### 1. Requirements File

Create a `requirements.txt` file with the following content:

```
python-dotenv
langchain
langchain-openai
langgraph
```

### 2. Environment File

Create a `.env` file in the root of the project. You can add any API keys or other secrets your custom tools might need here.

```
# Example for a hypothetical weather tool
WEATHER_API_KEY="your_secret_api_key_here"
```

## How to Run

To interact with the agent, you need an entrypoint script. Create a file named `main.py` and add the following code:

```python
from agent_engine import get_agent_executor
from tool import tools_list # Assuming tools_list is in tool.py

if __name__ == "__main__":
    # Get the configured agent executor
    agent_executor = get_agent_executor(tool_list=tools_list)

    print("Welcome to the Local ReAct AI Agent. Type 'exit' to quit.")

    while True:
        try:
            # Get user input
            user_input = input("You: ")
            if user_input.lower() == 'exit':
                break

            # The agent executor can be invoked with a dictionary
            # containing the input message.
            response = agent_executor.invoke(
                {"messages": [("user", user_input)]}
            )

            # The final answer is in the 'messages' list.
            # The last message is from the AI.
            ai_message = response['messages'][-1]
            if hasattr(ai_message, 'content'):
                print(f"Agent: {ai_message.content}")
            else:
                print(f"Agent: {ai_message}")

        except Exception as e:
            print(f"An error occurred: {e}")

```

Now, run the `main.py` script from your terminal:

```bash
python main.py
```

## Customization: Adding Tools

The agent is only as capable as its tools. To add a new tool:

1.  Open the `tool.py` file.
2.  Import the `@tool` decorator from `langchain.tools`.
3.  Define a Python function and decorate it with `@tool`. Make sure to include a good docstring, as the agent uses it to understand what the tool does.

**Example `tool.py`:**

```python
from langchain.tools import tool

@tool
def get_word_length(word: str) -> int:
    """Returns the length of a word."""
    return len(word)

# The agent_engine.py will import this list
tools_list = [get_word_length]
```