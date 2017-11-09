# tectonic
The use case library for Android

## Why would I need this?
You need Tectonic if you think you are one of the below
- You feel deep inside that the login, registration or splash screens are not your root screens and they should not decide your flow :)
- You want to to develop business logic that is independent of the UI flow.
- You know that the database is not the center of the universe.
- You believe that business logic should drive your app flow and not the UI.
- You believe that modern "Agile" development has destroyed good architecture.
- You enjoy writing business use cases and understand their importance for any software.

### What does it do?
Tectonic is a library that provides an abstraction and a management ground for developing business use cases programmatically independent of other aspects of the project itself, like the UI, persistence or services. 
It is just pure business logic.

You can subclass the UseCase class, creating your project use cases. The use cases you will write will almost be identical to a use case written by an architect, which means you have your use cases inside your code and not just in a document that can be neglected, forgotten or outdated.

When you write a use case, you start thinking of the preconditions to execute this use case. Tectonic provides the functionality that enables you to precondition your use case execution. This preconditioning is the key for flow control and will make your business logic in control and not the UI.

## Let me give you a simple example
Lets say you have a banking app, what is your main use case?
Definetly it is not the authentication/login use case, right? The Login use case is just a superimposed functionality that leads you to your main use case but it is not the main use case. The main use case here is to see your bank accounts, lets call it the ShowMyAccounts use case.
So why should the login use case (or the login screen) decides the control of your flow to lead you to the ShowMyAccounts (or the accounts screen)

Using Tectonic there is a new way of looking into this. Lets see how would we design this using the library
This is a hypothetical example just to clarify the idea

- You main entry is now the Home/Landing/MyAccounts screen (not the Splash or Login screens), this is of course defined in your manifest file.
- MyAccounts/Main screen will execute our entry/main use case when it starts.
- The main use case has a precondition, that the user should be logged in/authenticated in order to retrieve the user's accounts.
- The precondition in the ShowMyAccounts use case blocks itself and triggers the execution of the Login use case.
- The Login use case requires input from the outside world in the form of a password, so it blocks as well and requests the input it needs. 
- This login input request is captured by the Main screen which opens the Login screen.
- Once the user submits his password the Login screen will execute the pending Login use case again with password entered.
- The Login use case does it thing and authenticate the user, once the use case is completed the Login screen dismisses itself.
- Then the control goes back to the ShowMyAccounts use case which starts loading the user accounts.

In the same way we can make the Login use case preconditioned by the Registration use case and so on. The idea here is to give the use cases the business flow control and from there the UI reacts to it.

One cool thing here is the segregation of the UI and the use case. The case is oblivios about the existence of the UI.
Lets say the Registration is one screen which executes the Registration use case when the user submits his registration data.
Your UI designed decided he wants to split the Registration screen into screens. The good thing here is that the Registration use case will not care about that, it will send you the request for the missing input, which you can use to navigate to the second Registration screen.
The second registration screen can then execute the use case with all the inputs required by the Registration use case.

There is an PoC module in this project showing this example in code.

# Architecture
- 

# The uniqueness of this library

## Features

- Subscriptions

- Caching

- Chaining

- Callbacks
