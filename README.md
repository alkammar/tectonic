- The whole idea this framework is built on the assumption that business logic does not change frequently in a mature industry. At least not as frequent as the IO logic, like UI designs, backend services ... etc. Understand that lead to the creation of this framework to try to provide a good way of separation without compromising on interaction. 

- The most elegant way of defining business logic (in my opinion) is (and always was) use cases. Currently not used as much in application design, this framework aims to bring back use case design in applications by mirroring the use case in code.
 
- The use case abstraction aims to provide a way to mirror a use cases in code, providing means to define essential parts of it like triggers, primary actors, secondary actors, preconditions, main scenario and alternate scenarios. It is a different perspective of clean architecture, with a main goal to encapsulate business flow irrespective of the platform. Achieving that will allow shipping the use cases as separate module(s) that can preserve business logic from IO changes (e.g. UI, backend, storage ... etc). 

- A more ambitious goal is to reuse the use case logic across different applications and clients who operate within the same industry.
