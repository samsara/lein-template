# {{name}}

A Samsara's streaming app designed to ... well, that part is up to you.

## Usage

### Build

You can build the app with:

    lein do clean, compile :all

### Test

You can test your streaming logic with:

    lein midje

You can run tests automatically while coding with:

    lein midje :autotest


### Build executable

You can build a executable jar with:

    lein do clean, bin


### Run

Start a local cluster with:

    docker-compose up

When it is up and running you can start the streaming with:

    lein do clean, bin
    ./target/{{name}}-0.1.0-SNAPSHOT config/config.edn

or in development run with:

    lein run config/config.edn


Finally you can send events to:

    cat <<EOF | curl -i -H "Content-Type: application/json" \
                    -H "X-Samsara-publishedTimestamp: $(date +%s999)" \
                    -XPOST "http://127.0.0.1:9000/v1/events" -d @-
    [
      {
        "timestamp": $(date +%s000),
        "sourceId": "test-device",
        "eventName": "game.started",
        "level": 1,
        "levelScore": $RANDOM
      }
    ]
    EOF

And see the events via [http://217.0.0.1:8000](http://217.0.0.1:8000).
The first time you'll need to set it up as described in [Quick start guide](http://samsara-analytics.io/docs/quick-start/).

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.