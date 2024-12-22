## Contributing

Please use the following workflow to contribute:

* Fork the repository
* Create a branch which accurately describes your fix or feature (prefix branches with `fix/`, `feat/`, `docs/`, etc). 
  * Please use signed commits if you can - you are contributing to the backbone of a cryptocurrency.
* Format your new source according to the eclipse-java-style.xml and checkstyle.xml files before you make a pull request. 
* Submit a pull request against **`develop`**

*Note:* A minimum of 1 approving reviews are required to merge. Squash merges are preferred unless there are very few commits to merge

## GitHub Codespaces and devcontainers

This repository contains a devcontainer definition and workspace settings file for Visual Studio Code (vscode) that will make
spinning up a fully capable developer machine easy. The devcontainer can be used locally on your own docker desktop installation,
and is also compatible with GitHub Codespaces. This offers two easy options to get started developing on the core node:

_**IMPORTANT NOTE: Please do not save any of your personal editor settings to the vscode file or devcontainer. VSCode has 3
separate layers of settings that can be used. Please do not save them to the Workspace option or your pull request may be denied.**_

The devcontainer contains a few pre-installed vscode extensions useful to developing on a remote, container-base image.

The .vscode/settings.json file contains a few workspace settings necessary to enable the checkstyle extension and use
the eclipse-java-style.xml file by default.

### GitHub Codespaces - Recommended to Start
The easy way to get started. All you need to do is fork this repository, click on the green Code button, then the Codespaces tab,
and finally click the + button to launch a new codespace. You can reuse this codespace so long as you don't delete it, but as long
as you commit and sync your changes with git, you can destroy and rebuild it as often as you'd like. Follow the instructions above to
actually submit your contributions.

### Local devcontainer
To use a local devcontainer, you first need docker desktop installed. Once installed, ensure your local copy of vscode has the
[Remote Development](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.vscode-remote-extensionpack) extension
installed. After that, you can use CTRL-SHIFT-P and type 'Dev Containers' to find the option to reopen your repository in a
devcontainer. VSCode may prompt you automatically, and you can use that option as well.
