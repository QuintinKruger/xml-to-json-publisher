# Running the Application
provide the environment variables:
- ASYNC_POOL_SIZE - number of threads that can process XML files at one time
- DISK_SAVER_PATH - parent directory where output produced by service exists
The application can then be run from the command line using the spring boot plugin - `mvn spring-boot:run`

# Claude Code Disclaimer 
Claude code was used to assist me in the development of this application. Prompts asked and responses have been documented in [SOLUTION.md](SOLUTION.md#claude-code-prompts-and-responses)

# Documentation
- A process flow showcasing the process of converting XML files to normalized JSON files and RAG read text files can be found at [process-flow](docs/process_flow-XML_to_JSON_Publisher___Process_Flow.png)
- A demonstration video has also been included walking the reviewer through the process flow as well as the running of the application along with the triggering of the /upload-xml POST endpoint and the output produced from such an interaction - [video-demo](https://www.loom.com/share/f74e3ddcff8941b7b36a3f482dcffa60)

# Code Considerations 
A number of decisions were made during the development of this application and the reasons for them - these have been captured in [SOLUTION.md](SOLUTION.md#decisions-made-)

# Task 3
For all the points requested to be discussed as part of Task 3 - please see the relevant sections in [SOLUTION.md](SOLUTION.md) 