# Samsara Leiningen Template

The Samsara template has everything you need to get a processing
pipeline up and running quickly. The template provides you with a
starter processing core, a main, a test, and docker files to deploy
your project locally.

## Usage

To create a new Samsara project, run:

    lein new samsara <project-name>

By default, the new project will use the latest stable release version
of Samsara. If you wish to use a specific version, you can set the
version by running:

    lein new samsara <project-name> --with-version <version-number>

To use the latest release including snapshot, add the `--with-snapshot` argument:

    lein new samsara <project-name> --with-snapshot

## License

Copyright © 2015 Samsara's authors.

Distributed under the Apache License v 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
