# Camel K Knative Example

This example demonstrates the idiomatic way of using Camel K in Knative for building event-driven applications. It leverages the Knative eventing broker as 
the central point that lets various services communicate via event pub/sub. It also shows how Camel K can be used for connecting the Knative event mesh with external systems, with integrations that can play the roles of "event source" or "event sink".

## Scenario

The example shows a simplified **trading system** that analyzes price variations of **Bitcoins** (BTC / USDT),
using different prediction algorithms, and informs downstream services when it's time to **buy** or **sell** bitcoins (via CloudEvents).
It uses **real data** from the bitcoin exchange market, obtained in real time via the Camel `XChange` component.

![Diagram](docs/Diagram.png)

The architecture is composed of the following Camel K integrations:

- **market-source**: creates a live feed of **BTC/USDT** events, from the Bitcoin exchange market, containing the current value of a bitcoin and related information.
- **quarkus-ml**: an existing Quarkus-based machine learning service that we want to leverage.
- **prediction-bridge**: a Camel K bridge that will transform data to invoke the Quarkus algorithm and publish any suggested action to the broker.
- **telegram-sink**: this Camel K integration listens to buy/sell events from the `prediction-bridge` and transforms them into Telegram messages targeting a predefined chat room.
- **Telegram**: this represents an external service built by another team that needs suggestions from the predictor but it needs to receive them via some custom API (the Telegram Bot APIs).

All Camel K integrations described above (except the `market-source` which needs to poll the market for new data), are **"serverless"**, meaning that 
they scale down to zero when they don't receive new events or requests.

This means that the **whole infrastructure will not consume resources during the closing hours of the stock market** (unfortunately you won't be able to see this in the demo, because the Bitcoin market never closes, but we will simulate it).

## Before you begin

Make sure you check-out this repository from git and open it with [VSCode](https://code.visualstudio.com/).

Instructions are based on [VSCode Didact](https://github.com/redhat-developer/vscode-didact), so make sure it's installed
from the VSCode extensions marketplace.

From the VSCode UI, right-click on the `readme.didact.md` file and select "Didact: Start Didact tutorial from File". A new Didact tab will be opened in VS Code.

Make sure you've opened this readme file with Didact before jumping to the next section.

## Preparing the cluster

This example can be run on any OpenShift 4.3+ cluster or a local development instance (such as [CRC](https://github.com/code-ready/crc)). Ensure that you have a cluster available and login to it using the OpenShift `oc` command line tool.

You need to create a new project named `camel-knative` for running this example. This can be done directly from the OpenShift web console or by executing the command `oc new-project camel-knative` on a terminal window.

You need to install the Camel K operator in the `camel-knative` project. To do so, go to the OpenShift 4.x web console, login with a cluster admin account and use the OperatorHub menu item on the left to find and install **"Red Hat Integration - Camel K"**. You will be given the option to install it globally on the cluster or on a specific namespace.
If using a specific namespace, make sure you select the `camel-knative` project from the dropdown list.
This completes the installation of the Camel K operator (it may take a couple of minutes).

When the operator is installed, from the OpenShift Help menu ("?") at the top of the WebConsole, you can access the "Command Line Tools" page, where you can download the **"kamel"** CLI, that is required for running this example. The CLI must be installed in your system path.

Refer to the **"Red Hat Integration - Camel K"** documentation for a more detailed explanation of the installation steps for the operator and the CLI.

You can use the following section to check if your environment is configured properly.

### Installing OpenShift Serverless

This demo also needs OpenShift Serverless (Knative) installed and working on the cluster.

You need to install the **OpenShift Serverless** operator from Operator Hub in your OpenShift installation, 
then use it to install both **Knative Serving** and **Knative Eventing**.

Refer to the OpenShift Serverless operator documentation for instructions on how to completely install it on your cluster.

### Installing Knative Camel Sources

Knative Camel Sources are an addon for Knative that allows using Camel K integrations as 
standard Knative sources.

You need to install the **Knative Camel Sources** operator from Operator Hub in your OpenShift installation.

## Checking requirements

<a href='didact://?commandId=vscode.didact.validateAllRequirements' title='Validate all requirements!'><button>Validate all Requirements at Once!</button></a>

**OpenShift CLI ("oc")**

The OpenShift CLI tool ("oc") will be used to interact with the OpenShift cluster.

[Check if the OpenShift CLI ("oc") is installed](didact://?commandId=vscode.didact.cliCommandSuccessful&text=oc-requirements-status$$oc%20help&completion=Checked%20oc%20tool%20availability "Tests to see if `oc help` returns a 0 return code"){.didact}

*Status: unknown*{#oc-requirements-status}


**Connection to an OpenShift cluster**

You need to connect to an OpenShift cluster in order to run the examples.

[Check if you're connected to an OpenShift cluster](didact://?commandId=vscode.didact.requirementCheck&text=cluster-requirements-status$$oc%20get%20project$$NAME&completion=OpenShift%20is%20connected. "Tests to see if `kamel version` returns a result"){.didact}

*Status: unknown*{#cluster-requirements-status}

**Apache Camel K CLI ("kamel")**

Apart from the support provided by the VS Code extension, you also need the Apache Camel K CLI ("kamel") in order to 
access all Camel K features.

[Check if the Apache Camel K CLI ("kamel") is installed](didact://?commandId=vscode.didact.requirementCheck&text=kamel-requirements-status$$kamel%20version$$Camel%20K%20Client&completion=Apache%20Camel%20K%20CLI%20is%20available%20on%20this%20system. "Tests to see if `kamel version` returns a result"){.didact}

*Status: unknown*{#kamel-requirements-status}

**Knative installed on the OpenShift cluster**

The cluster also needs to have Knative installed and working.

[Check if the Knative is installed](didact://?commandId=vscode.didact.requirementCheck&text=kservice-project-check$$oc%20api-resources%20--api-group=serving.knative.dev$$kservice%2Cksvc&completion=Verified%20Knative%20services%20installation. "Verifies if Knative is installed"){.didact}

*Status: unknown*{#kservice-project-check}

### Optional Requirements

The following requirements are optional. They don't prevent the execution of the demo, but may make it easier to follow.

**VS Code Extension Pack for Apache Camel**

The VS Code Extension Pack for Apache Camel by Red Hat provides a collection of useful tools for Apache Camel K developers,
such as code completion and integrated lifecycle management. They are **recommended** for the tutorial, but they are **not**
required.

You can install it from the VS Code Extensions marketplace.

[Check if the VS Code Extension Pack for Apache Camel by Red Hat is installed](didact://?commandId=vscode.didact.extensionRequirementCheck&text=extension-requirement-status$$redhat.apache-camel-extension-pack&completion=Camel%20extension%20pack%20is%20available%20on%20this%20system. "Checks the VS Code workspace to make sure the extension pack is installed"){.didact}

*Status: unknown*{#extension-requirement-status}


## 1. Preparing the project

We'll connect to the `camel-knative` project and check the installation status.

To change project, open a terminal tab and type the following command:


```
oc project camel-knative
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20project%20camel-knative&completion=Project%20switch. "Opens a new terminal and sends the command above"){.didact})


We should now check that the operator is installed. To do so, execute the following command on a terminal:


Upon successful creation, you should ensure that the Camel K operator is installed:

```
oc get csv
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20get%20csv&completion=Checking%20Cluster%20Service%20Versions. "Opens a new terminal and sends the command above"){.didact})


When Camel K is installed, you should find an entry related to `red-hat-camel-k-operator` in phase `Succeeded`.

You can now proceed to the next section.

## 2. Enabling the Knative Eventing Broker

The central piece of the event mesh that we're going to create is the Knative Eventing broker. It is a publish/subscribe entity 
that Camel K integrations will use to publish events or subscribe to it in order to being triggered when events of specific
types are available. Subscribers of the eventing broker are Knative serving services, that can scale down to zero when no
events are available for them.

To enable the eventing broker, we create a `default` broker in the current namespace using namespace labeling:

```
oc label namespace camel-knative knative-eventing-injection=enabled
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20label%20namespace%20camel-knative%20knative-eventing-injection%3Denabled&completion=Created%20Knative%20Broker. "Opens a new terminal and sends the command above"){.didact})

## 2. Publish the Quarkus example ML service

This demo simulates the existence of a Quarkus Machine Learning service with a simple API that accepts numbers and replies 
occasionally with suggested actions (buy or sell).

We'll create the project using the ([machine-learning/service.yaml](didact://?commandId=vscode.open&projectFilePath=machine-learning/service.yaml "Opens the file"){.didact}) file:


```
kubectl apply -f machine-learning/service.yaml
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kubectl%20apply%20-f%20machine-learning/service.yaml&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})


## 4. Push Bitcoin market data to the mesh

We'll create a ([market-source.yaml](didact://?commandId=vscode.open&projectFilePath=market-source.yaml "Opens the file"){.didact}) integration
with the role of taking live data from the Bitcoin market and pushing it to the event mesh, using the `market.btc.usdt` event type:


```
kubectl apply -f market-source.yaml
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kubectl%20apply%20-f%20market-source.yaml&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})


```
stern market -c integration
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$stern%20market%20-c%20integration&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})


The command above will run the integration and wait for it to run, then it will show the logs in the console.

[**To exit the log view**, just click here](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=camelTerm&completion=Camel%20K%20basic%20integration%20interrupted. "Interrupt the current operation on the terminal"){.didact} 
or hit `ctrl+c` on the terminal window. The integration will **keep running** on the cluster.

## 5. Run the prediction adapter

We're going to run an adapter that will forward the current value of BTC vs USDT to the Quarkus service and publish its reply (if available):

```
kamel run PredictionBridge.java --logs
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kamel%20run%20PredictionBridge.java%20--logs&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})

The command above will deploy the integration and wait for it to run, then it will show the logs in the console.

[**To exit the log view**, just click here](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=camelTerm&completion=Camel%20K%20basic%20integration%20interrupted. "Interrupt the current operation on the terminal"){.didact} 
or hit `ctrl+c` on the terminal window. The integration will **keep running** on the cluster.

## 6. Connecting an external application (Telegram)

We'll simulate the presence of existing investors that are not directly connected to the mesh, but they can be reached via Telegram.

Create a copy of the [telegram.properties.example](didact://?commandId=vscode.open&projectFilePath=telegram.properties.example "Opens the Telegram property file"){.didact} and name it `telegram.properties`. You need to put in that file the **Telegram authorization token** for your Telegram bot. You can use the Telegram bot father to obtain it.
Other than the authorization token, you also need to set the **unique identifier of the Telegram chat** where you want to publish messages to.


Now we can deploy the [TelegramSink.java](didact://?commandId=vscode.open&projectFilePath=TelegramSink.java "Opens the investor service adapter sink definition"){.didact} integration, that will bring events from the "better" predictor right into the Telegram chat, after a simple transformation:


```
kamel run TelegramSink.java -w
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kamel%20run%20TelegramSink.java%20-w&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})


Once the Telegram sink is running, you can look at the external service logs to see if it's receiving recommendations.
The command for printing the logs is:

```
kamel logs telegram-sink
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kamel%20logs%20telegram-sink&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})

[**To exit the log view**, just click here](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=camelTerm&completion=Camel%20K%20basic%20integration%20interrupted. "Interrupt the current operation on the terminal"){.didact} 
or hit `ctrl+c` on the terminal window.

You can alternatively follow the logs using the IDE plugin, by right clicking on a running integration on the integrations view.

## 7. When the market closes...

Bitcoin market never closes, but closing hours are expected to be present for standard markets.
We're going to simulate a closing on the market by stopping the source integration.

When the **market closes** and updates are no longer pushed into the event mesh, **all downstream services will scale down to zero**.
This includes the two prediction algorithms, the two services that receive events from the mesh and also the external investor service.

To simulate a market close, we will delete the `market-source`:

```
kubectl delete camelsource market-source
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kubectl%20delete%20camelsource%20market-source&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})


To see the other services going down (it will take **about 2 minutes** for all services to go down), you can repeatedly run the following command:


```
oc get pod
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20get%20pod&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})

At the end of the process, **no user pods will be running**.

To simulate now a reactivation of the market in the morning, you can create again the `market-source`:

```
kubectl apply -f market-source.yaml
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kubectl%20apply%20-f%20market-source.yaml&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})


Pods now will start again to run, one after the other, as soon as they are needed:

```
oc get pod
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20get%20pod&completion=Executed%20command. "Opens a new terminal and sends the command above"){.didact})

This terminates the example.

## 8. Uninstall

To cleanup everything, execute the following command:

```oc delete project camel-knative```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20delete%20project%20camel-knative&completion=Removed%20the%20project%20from%20the%20cluster. "Cleans up the cluster after running the example"){.didact})
