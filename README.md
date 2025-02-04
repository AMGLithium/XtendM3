# Introduction 
XtendM3 is a customization tool and service provided by Infor for modifying and extending M3 Business Engine logic in the Cloud.
For more information, please refer to the Infor XtendM3 [documentation.](https://infor-cloud.github.io/xtendm3/)

# Getting Started

1. **Install Java 17**:
   - Download the Java 17 JDK from the Oracle website.
   - Run the installer and follow the instructions.

2. **Set Java Environment Variables**:
   - Open the Start Menu, search for "Environment Variables", and select "Edit the system environment variables".
   - In the System Properties window, click on the "Advanced" tab and then click "Environment Variables".
   - Under "System variables", click "New" and add `JAVA_HOME` with the path to your JDK installation (e.g., `C:\Program Files\Java\jdk-17`).
   - Find the `Path` variable, click "Edit", and add `%JAVA_HOME%\bin`.

3. **Install Maven**:
   - Download Maven from the Apache Maven website.
   - Extract the downloaded archive to a directory (e.g., `C:\Program Files\Apache\maven`).
   - Add the `MAVEN_HOME` environment variable pointing to the Maven directory.
   - Edit the `Path` variable and add `%MAVEN_HOME%\bin`.

4. **Verify Installations**:
   - Open a Command Prompt and run `java -version` to check Java installation.
   - Run `mvn -version` to check Maven installation.

### Install Visual Studio Code

1. **Download and Install VS Code**:
   - Download Visual Studio Code from the official website.
   - Run the installer and follow the instructions.

2. **Install Java Extension Pack**:
   - Open Visual Studio Code.
   - Go to the Extensions view by clicking on the Extensions icon in the Activity Bar on the side of the window.
   - Search for "Java Extension Pack" and install it.

3. **Install Groovy Extension**:
   - In the Extensions view, search for "Groovy" and install the Groovy extension.

4. **Install XML Extension (Optional)**:
   - In the Extensions view, search for "XML" and install the XML extension.   

### Setting Up the Project

1. **Clone the Repository**:
   - **Using Git**:
     - Open a terminal and run:
       ```sh
       git clone <repository-url>
       cd <repository-directory>
       ```
   - **Using Azure DevOps**:
     - Open Visual Studio Code.
     - Click on the Source Control icon in the Activity Bar.
     - Click on "Clone Repository" and select "Azure DevOps".
     - Follow the prompts to authenticate and select your repository.

2. **Open the Project in VS Code**:
   - Open Visual Studio Code.
   - Go to `File > Open Folder` and select your project directory.

3. **Build the Project**:
   - Open a terminal in VS Code and run:
     ```sh
     mvn clean install
     ```

### Changing the Repository for a New Project

This repository is intended to be used as a template. When starting a new project, you should change the repository to your new project's repository. Follow these steps:

1. **Remove the Existing Remote**:
   - Open a terminal in your project directory and run:
     ```sh
     git remote remove origin
     ```

2. **Add the New Remote**:
   - Add your new repository as the remote by running:
     ```sh
     git remote add origin <new-repository-url>
     ```

3. **Push to the New Repository**:
   - Push your local changes to the new repository:
     ```sh
     git push -u origin main
     ```
### Examples

For reference and guidance, you can check out the examples provided in the `examples` branch of this repository. These examples demonstrate various use cases and scenarios for working with Infor XtendM3 extensions. Feel free to explore and use them as a basis for your own implementations.

You are now ready to start working with Infor XtendM3 extensions in your development environment. Happy coding!