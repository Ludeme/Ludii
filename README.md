<img align="right" src="./resources/ludii-logo-64x64.png">

# The Ludii General Game System

<!--[![Documentation Status](https://readthedocs.org/projects/ludiitutorials/badge/?version=latest)](https://ludiitutorials.readthedocs.io/en/latest/?badge=latest)-->
[![Build Status](https://travis-ci.com/Ludeme/Ludii.svg?branch=master)](https://travis-ci.com/Ludeme/Ludii)
![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)
[![twitter](https://img.shields.io/twitter/follow/ludiigames?style=social)](https://twitter.com/intent/follow?screen_name=ludiigames)

Ludii is a general game system being developed as part of the [ERC-funded Digital Ludeme Project (DLP)](http://ludeme.eu/). This repository hosts the publicly available source code for Ludii. A precompiled build (Ludii.JAR) can be downloaded from [Ludii's downloads page](https://ludii.games/download.php).

## Requirements

Working with Ludii's source code requires Java Development Kit (JDK) version 8 or higher.

## Getting Started

After (optionally forking) and cloning this repository, we recommend importing all projects into the Eclipse IDE. The main method to launch Ludii from your IDE is located in `/Ludii/PlayerDesktop/src/app/StartDesktopApp`. At this time we do not use any more sophisticated build tools (such as Maven) in our day-to-day programming with Ludii. There are some relatively simple Ant build scripts, but we only use these -- specifically, the `/Ludii/PlayerDesktop/build.xml` script -- for generating the releases published on the [Ludii downloads page](https://ludii.games/download.php).

**Note on IDEs**: Other IDEs than Eclipse should ideally work as well, but we have no extensive experience working with Ludii in other IDEs, and are aware of at least some issues. For example, some parts of Ludii's code assume that, when launching Ludii from your IDE, that the current working directory is the one of the module containing the `main` method (i.e., `/Ludii/PlayerDesktop`). This is the case in Eclipse, but does not (by default) appear to be the case in some other IDEs such as IntelliJ. If you prefer working with different IDEs and are able to write a clear set of instructions for that IDE, we would be happy to see it in [a new Pull Request](https://github.com/Ludeme/Ludii/pulls)!

## Other Resources

We have various other resources available at the following links:

- [Ludii Tutorials](https://ludiitutorials.readthedocs.io/en/latest/): various tutorials on aspects ranging from game design (using Ludii's game description language) to programming with Ludii as a programmatic library. The source files for these tutorials are [also available on GitHub](https://github.com/Ludeme/LudiiTutorials).
- [Ludii Documentation](https://ludii.games/download.php): the Ludii downloads page provides links to additional documentation, such as the [Ludii User Guide](https://ludii.games/downloads/LudiiUserGuide.pdf), [Ludii Game Logic Guide](https://ludii.games/downloads/LudiiGameLogicGuide.pdf), [Ludii Language Reference](https://ludii.games/downloads/LudiiLanguageReference.pdf), and [DLP Database Guide](https://ludii.games/downloads/DLP_Database_Guide.pdf).
- [Ludii Example AI](https://github.com/Ludeme/LudiiExampleAI): an example project for implementing Java-based AIs that can play Ludii games.
- [Ludii Python AI](https://github.com/Ludeme/LudiiPythonAI): an example project for implementing Python-based AIs that can play Ludii games.
- [Ludii AI Competition](https://github.com/Ludeme/LudiiAICompetition): a supporting project for Ludii AI competitions.
- [Ludii AI](https://github.com/Ludeme/LudiiAI): a repository used to hold the source code of built-in Ludii AIs before the entirety of the project was open-sourced. This repository is now deprecated, since the source code is also included in this repo.

## Contributing Guidelines

While we of course cannot guarantee that we will accept every suggested change or contribution, in principle we welcome contributions and are excited to see what you come up with! Please send contributions on GitHub as [new Pull Requests](https://github.com/Ludeme/Ludii/pulls), and provide brief descriptions of what has changed and in what ways these changes improve Ludii (or other aspects of the repo, such as documentation). Please ensure that any new or changed code follows the same code style as the rest of the repository.

**Note**: pull requests should be used for *code* or *documentation* contributions, but **not** for *new games* (i.e., `.lud` files). We prefer that new games are [submitted through our forums](https://ludii.games/forums/forumdisplay.php?fid=23).

## Citing Information

When using Ludii's source code in any publications, please cite our paper describing Ludii: https://ecai2020.eu/papers/1248_paper.pdf

The following `.bib` entry may be used for citing the use of Ludii in papers:


	@inproceedings{Piette2020Ludii,
            author      = "{\'E}. Piette and D. J. N. J. Soemers and M. Stephenson and C. F. Sironi and M. H. M. Winands and C. Browne",
            booktitle   = "Proceedings of the 24th European Conference on Artificial Intelligence (ECAI 2020)",
            title       = "Ludii -- The Ludemic General Game System",
            pages       = "411-418",
            year        = "2020",
            editor      = "G. De Giacomo and A. Catala and B. Dilkina and M. Milano and S. Barro and A. Bugarín and J. Lang",
            series      = "Frontiers in Artificial Intelligence and Applications",
            volume      = "325",
	    publisher	= "IOS Press"
    }

## Contact Info

The preferred method for getting help with troubleshooting, suggesting or
requesting additional functionality, or asking other questions about Ludii's source code, 
is posting a message on the [Ludii Forum](https://ludii.games/forums/).
Alternatively, the following email address may be used: `ludii(dot)games(at)gmail(dot)com`.

## Acknowledgements

This repository is part of the European Research Council-funded Digital Ludeme Project (ERC Consolidator Grant \#771292) run by Cameron Browne at [Maastricht University's Department of Advanced Computing Sciences](https://www.maastrichtuniversity.nl/dacs). 

<a href="https://erc.europa.eu/"><img src="./resources/LOGO_ERC-FLAG_EU_.jpg" title="Funded by the European Research Council" alt="European Research Council Logo" height="384"></a>
