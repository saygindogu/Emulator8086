# Emulator8086
An Emulator for 8086 architecture.
[Geeks For Geeks Explanation](https://www.geeksforgeeks.org/architecture-of-8086/) of this architecture is a suggested read.

# How to run the project
It's a gradle project. Please follow the instructions on google on `how to run gradle projects`. 

# History
This was a project for fun that I implemented I don't know how many years ago.

It was put to github by me in Nov 2015 and then there is one more commit which was in April 2016.

In April 2022, I decided to have fun with this one more time. Document this properly and maybe make it somehow useful?

# Exploration in 2022

- When I pulled the repository I looked at the files such as `ali`, `asd.asm` etc. I have no idea what they are doing now but they are source code for the program.
- I saw some IDE related files. I decided to remove them from the repo once and for all.
- I saw no `.gitignore` file, hence I will add it shortly. I saw a `bin` folder which was committed.
- This repo will surely endure a rebase and history re-write.
- I have seen some lecture notes in the `emulatorDosyalari` directory.
- It was now time to make the project compile.
- It did not compile for some reason :)
- I made this a gradle project and added the main class and all sorts of things as usual.
- The files were not UTF-8 encoded for some reason and there were funny characters. A quick find and replace fixed the issue.
- I needed to remove some unused imports and voila, the application was running.
- The UI seems a bit oldie, but it is really nice actually. There is a app bar and you can do some stuff.
- You can open a file, and run it. 
- Though there's no button for stopping so if the assembly code results in infinite running, it'll run forever.
- There's even Options button where we can change the representation of the memory and addresses to binary hex and decimal options. Neat!!
- There's also **help!** section in the options list. It gives a website that doesn't work. I replaced it with something that works.
- Seems pretty decent to me. I'm proud of myself actually.
- I tidied up the place and will commit soon. And probably rebase and erase all history haha.

# Code Rewiew
- Coming soon.
