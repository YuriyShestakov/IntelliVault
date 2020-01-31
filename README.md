# IntelliVault

A plugin for IntelliJ IDEA to interact with JCR repositories via the FileVault tool which is packaged with Adobe Experience Manager.

This plugin is largely based upon, and liberally borrows from, [VaultClipse](http://vaultclipse.sourceforge.net/) which is a plugin for the Eclipse IDE for interacting with FileVault.

The plugin can be found in the [JetBrains IDEA Plugin Repository](http://plugins.jetbrains.com/plugin/7328)

## Supported product versions

The *IntelliVault* plugin is currently supported on the following Intellij products:

* Intellij IDEA 2017.1++ Community/Ultimate

## Installation

To install the plugin using the Intellij built-in plugin management dialog, go to **Preferences** > **Plugins** > **Browse Repositories**, type *Intellivault* and click the **Install** button.

NOTE: If after installing the plugin and restarting the IDE you don't see the **IntelliVault** option under **Tools** then your version is most likely not supported.

## Setting up Vault CLI

IntelliVault uses the [Filevault CLI](https://docs.adobe.com/content/help/en/experience-manager-65/developing/devtools/ht-vlttool.html) under the covers to transfer content between IDEA and your AEM repository.  This is a hard dependency, and requires downloading and unpacking Filevault CLI v3.2+ before you can configure the plugin.

You can download the filevault CLI from https://repo1.maven.org/maven2/org/apache/jackrabbit/vault/vault-cli/. Be sure you download the binary artifact, version 3.2 or greater, e.g. https://repo1.maven.org/maven2/org/apache/jackrabbit/vault/vault-cli/3.4.2/vault-cli-3.4.2-bin.zip.  Once the download has completed, locate it in your Downloads directory and unpack it to the directory of your choice.

## Configuration

Oopen the plugin configuration dialog accessible via **Preferences** > **Tools** > **IntelliVault** and set the following properties.

- **Vault Directory**: Set this to the directory where you unpacked Filevault, ie. `/Users/myuser/dev/tools/vault/vault-cli-3.1.38/bin`
- **Repository**: See `Multi-Repository Configuration` below
- **Show Operation Confirmation Dialogs**: If checked, IntelliVault will prompt you to comfirm each operation.  Uncheck this to remove those confirmations
- Other properties are optional and shouldn't require changes, but should be self-explanatory if/when changes are required

### Multi-Repository Configuration

IntelliVault allows you to configure and manage multiple repositories.  If more than one repo is configured, you will be prompted to select a repo for each operation.  If only one repo exists, that repo will be used without any prompt.

For each repo, you must set the following:

- **Repository Name**: Friendly name for this repo.
- **CRX Repository URL**: URL for the repo, i.e. http://localhost:4502
- **Username**: Username for connecting to the repository, ie. admin
- **Password**: Password used for connecting to the repository, ie. admin.  **Note: the password is stored in plaintext.  It is therefore not recommended to use this plugin to connect to any instance other than for local development.**

When you first install the plugin, it will load 2 pre-configured repositories:

- an author instance running on localhost:4502, with the default credentials of admin/admin
- a publish instance on localhost:4503, with the default credentials of admin/admin
