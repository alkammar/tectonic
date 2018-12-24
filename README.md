# tectonic
A way to write use cases in code for Android (for now).
It is a tool to implement requirements (abstract and concrete) by providing a way to write down the use case elements in code and executing that code.
A way to directly bind business requirements and implementation. And as a result the code takes a good step becoming a business documentation.

## Problems That I Have Been Facing
While designing and implementing software/application/component in recent times (8 years) I always end up with the same project ingredients.

#### Agile
This has become the trend method to delivering software for quiet some time now. 
I even had a chat with a friend who works in embedded systems development in Germany and told me that they use Agile. Which was a surprise to me and still to him, as he thinks it is used just because everyone is using it now and they should not be falling behind (this is actually an issue I see in software development we see a lot with new tools/methods becoming popular each year without analysing the real benefits to the business). 

I beleive Agile was adopted to solve a main issue in software that has been seen while delivering. A client provide requirement, vendor says it would take x months to deliver including desing and development. After x months, the client checks the delivables and says that is not what we wanted, classic client!
So the Agile came to solve this problem by envolving the client in the development process to keep the delivery steered in the desired direction.
Also introducing a backlog to put all the client dreams - sorry requirements - in a controlled pipeline or else nothing will be delivered. If you tell me as a client I can come every 2 weeks and change my mind on what I want to do, I will do it every 2 weeks.

My problem with Agile is not the above (assuming the Agile process is implemented correctly), but with the fact that in Agile you are not given time to properly design the software. You are only given time to implement UI and call some services. The fast paced short burst (sprint) nature of Agile makes it hard to design a decent software with proper components and abstractions. Basically it is hard to design a component that outlasts the project scope and can be used in a another project (cross industries or even within the same industry). Don't ever think when I say components I mean utils!

This is why I think there is a growing popularity for tools that for instance makes it easier to integrate a backend service like Retrofit but nothing to make designing software more easier (one TecTocic's main aims).

#### User Stories and Wireframes
All I can say about user stories that they are cool ... from a one dimentional visual user perspective. They will give you minimum insight on how the application components interact with each other (or even what are the application components) to achieve the flow required. And you will end up either with screens or service calls that are doing business logic (later on how much business logic is actually there in the simplest applications) or a ridiculous component that looks like it was adopted from the streets that does not look like anything else in the project.

If you from a older era that experienced the majesty of use cases, you know how much they are more expressive than user stories although having the simplest visual design symbols, a stick man and an oval! 
With elements like:
- Triggers
- Preconditions
- Actors
- Sub use cases
- Main scenario
- Alternate scenarios
- Post conditions

If you have not seen or written a use case before I suggest reading about them first, they are so elegant.

#### Tight delivery dates
Nothing to say here, they just fuck things up more.

## What I Actually Wanted in my Software
#### Clean architecture
My main use of clean architecture is to make the business logic the driver seat, calling the shots, independant on any platform specific components and all other app components react to its actions. Not the other way around where you have the screens (which are platform specific component) control the actions flow.

#### Business abstraction
The business logic can be reused across different projects in the same industry if abstracted enough (and of course is  extendable). Furthermore that business logic can span cross industries if they are having the same use case, something like reset password, a use case that can be found in any client application that requires credentials.

The high level goal is to create a library of abstract and flexible business components that can be used in more than one project. That way we can kick off projects with 



Let me give an example of a feature that is implmented cross industries that you can find in many applications.
Resetting a user's password.
When loo

# Architecture
- 
