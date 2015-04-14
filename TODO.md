* in tmp/blueprint-muleserver.yaml, the run.dir has hard coded value of `run.dir: ~/mule-home/mule-standalone-3.6.1`
  Ideally, this would be something like this `run.dir: ~/mule-home/mule-standalone-${version}`, but it seems like you need to use Futures as
  there is no value ready for ${version} at the point run.dir is resolved.
