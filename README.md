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

5. **Configuring Git**:

   - **Install Git**:
      - Download Git from the official website.
      - Run the installer and follow the instructions.
      - During installation, select the option to "Git from the command line and also from 3rd-party software" to add Git to the PATH.

   - **Set Up Git**:
      - Open Git Bash or a terminal and configure your user name and email:
      ```sh
      git config --global user.name "Your Name"
      git config --global user.email "your.email@example.com"
      ```

   - **Verify Configuration**:
      - Check your configuration settings:
      ```sh
      git config --list
      ```

   -  **Authenticating with Azure DevOps**

      - Follow [this microsoft guide](https://learn.microsoft.com/en-us/azure/devops/repos/git/use-ssh-keys-to-authenticate?view=azure-devops) to learn how to configure the access to the repository



# Setting Up the Project

 ### Visual Studio Code ##
   1. **Download and Install VS Code**:
      - Download Visual Studio Code from the official website.
      - Run the installer and follow the instructions.

   2. **Install Java Extension Pack**:
      - Open Visual Studio Code.
      - Go to the Extensions view by clicking on the Extensions icon in the Activity Bar on the side of the window.
      - Search for "Java Extension Pack" and install it.

   3. **Install Groovy Extension**:
      - In the Extensions view, search for "code-groovy" and install the Groovy extension.

   4. **Install XML Extension (Optional)**:
      - In the Extensions view, search for "XML" and install the XML extension.   

   5. **Clone the Repository**:
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

      - **Open the Project in VS Code**:
         - Open Visual Studio Code.
         - Go to `File > Open Folder` and select your project directory.
         - Build the Project**:
            - Open a terminal in VS Code and run:
               ```sh
               mvn clean install
               ```
 ### Eclipse ##
   1. **Download and Install Eclipse**:
      - Download Eclipse IDE for Java Developers from the official website.
      - Run the installer and follow the instructions.

   2. **Install Maven Integration for Eclipse (m2e)**:
      - Open Eclipse.
      - Go to `Help > Eclipse Marketplace`.
      - Search for "Maven Integration for Eclipse" and install it.

   3. **Clone the Repository**:
      - **Using Git**:
         - Open a terminal and run:
            ```sh
            git clone <repository-url>
            cd <repository-directory>
            ```

   4. **Import the Project**:
      - Open Eclipse.
      - Go to `File > Import`.
      - Select `Maven > Existing Maven Projects` and click `Next`.
      - Browse to your project directory and click `Finish`.

   4. **Build the Project**:
      - Right-click on the project in the Project Explorer.
      - Select `Run As > Maven install`.

### Note on Dependency Connections

While Visual Studio Code is a powerful editor, it looks like there are issues with dependency connections when exploring and opening them. Eclipse, on the other hand, tends to handle these connections more reliably, making it a better choice for managing dependencies in this project.

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

### Final notes
A folder called extensions has been created to store the XtendM3 files, for reference and guidance, you can check out the examples provided in the `examples` branch of this repository. These examples demonstrate various use cases and scenarios for working with Infor XtendM3 extensions. Feel free to explore and use them as a basis for your own implementations.
You are now ready to start working with Infor XtendM3 extensions in your development environment. Happy coding!