const path = require('path');
const { workspace, ExtensionContext } = require('vscode');
const { LanguageClient, TransportKind } = require('vscode-languageclient/node');

let client;

function activate(context) {
    // Get the server path from configuration or use default
    const config = workspace.getConfiguration('modelphy');
    let serverPath = config.get('server.path');
    
    if (!serverPath) {
        // Use bundled server jar if no custom path specified
        serverPath = context.asAbsolutePath(path.join('server', 'modelphy-lsp-server.jar'));
    }
    
    const fs = require('fs');
    console.log('Server path:', serverPath);
    console.log('Server file exists:', fs.existsSync(serverPath));
    
    // Server options - the command to start the language server
    const serverOptions = {
        run: {
            command: 'java',
            args: ['-jar', serverPath],
            options: { cwd: path.dirname(serverPath) },
            transport: TransportKind.stdio
        },
        debug: {
            command: 'java',
            args: ['-jar', serverPath, '--debug'],
            options: { cwd: path.dirname(serverPath) },
            transport: TransportKind.stdio
        }
    };
    
    // Client options - define the document selector
    const clientOptions = {
        documentSelector: [{ scheme: 'file', language: 'modelphy' }],
        synchronize: {
            // Notify the server about file changes in the workspace
            fileEvents: workspace.createFileSystemWatcher('**/*.mphy')
        }
    };
    
    // Create the language client and start it
    client = new LanguageClient(
        'modelphy',
        'ModelPhy Language Server',
        serverOptions,
        clientOptions
    );
    
    // Start the client and add it to the subscriptions
    const disposable = client.start();
    context.subscriptions.push(disposable);
    
    console.log('ModelPhy language server started');
}

function deactivate() {
    if (!client) {
        return undefined;
    }
    return client.stop();
}

module.exports = {
    activate,
    deactivate
};