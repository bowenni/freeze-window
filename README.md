# Freeze Window

A Jenkins plugin that blocks the builds in freeze windows.

Given an array of cron expressions, wherein each cron specifies a window during which builds are delayed from building. Any builds queued during this time will remain queued until the freeze window is over.

Default: Nothing, Type: An array of strings of standard CRON syntax, e.g.["* * ? * 1", "0-23 0-59 * 1 ?"] contains two freeze windows. The first window blocks builds every Monday. The second window blocks builds in January.

## Getting Started on OS X

 - Install [Homebrew](http://brew.sh).
 - `brew install maven`

## Test

    mvn test

## Run

    ./build-fast.sh

## License

MIT, see LICENSE file.
