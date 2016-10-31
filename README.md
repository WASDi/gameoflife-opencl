# gameoflife-opencl

Just me playing around with OpenCL. Partly unorganized code with lots of notes to self in comments. Initially not intended to be published.

https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life

* org.wasd.gameoflife.**GameOfLifeGUI** - Runs Game of Life, with either CPU or OpenCL implementation.
* org.wasd.jocl.core.**OpenCLBase** - Attempt to abstractify a kernel, making implementations cleaner. Turned out quite well and subclasses don't need to import anything OpenCL specific (except sizeof, which is simple enough to not abstract away)
* org.wasd.jocl.**GuiForOpenCLWithImage** - Spawnws the "warp" kernel with an image as input. Not related to Game of Life.
* org.wasd.jocl.impl.**WithWebcamAsInputGUI** - Same as above but live webcam as input.

Benchmark when running 1000 iterations of a 1000x1000 grid
* CPU implementation: 34283ms
* OpenCL implementation 465ms

So 75x speedup, and 2.15 BILLION cell updates per second!

Read more here: https://www.reddit.com/r/OpenCL/comments/59zp0p/some_questions_about_my_first_opencl_project_a/
