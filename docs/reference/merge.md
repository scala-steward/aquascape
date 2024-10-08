{%
  pageid = DocsReferenceMerge
  helium.site.pageNavigation.keepOnSmallScreens = true
%}

# merge

@:todo(
This page is a reference doc for merge. It is not:
 - A tutorial on concurrency
 - A guide on how to fan-in datasources.
 
The user will consult this, assuming they already understand concurrency, when they want to know how merge behaves in different scenarios.
)

The `merge` operator combines a left and right stream concurrently.

This is a reference guide to `merge`. It describes:

 - The basic behaviour of `merge`.
 - The different `merge` operators.
 - The termination behaviour of each `merge` operator.
 - The exit cases of each stream on termination.
 - How `merge` operators propagate errors.
 - When resource finalizers are executed.
 
## Basic behaviour

The `merge` operator combines a left input stream and right input stream to produce a resulting stream. It pulls on both left and right streams concurrently.

The following examples merge two streams of characters. 

 - The `ab` stream outputs `'a'` and `'b'` at a given rate. It is always used as the left input stream.
 - The `xy` stream outputs `'x'` and `'y'` at a fixed rate. It is always used as the right input stream.

Experiment with different delays for `ab`. 

@:exampleWithInput(merge) {
  drawChunked = false
}

Note how decreasing the delay makes the `a` and `b` elements appear earlier in the output, while increasing it makes them appear later. The actual output order is non-deterministic.

Unlike sequential methods of combination, `merge` pulls on both its input streams when it is pulled on. This can be seen in the example for all non-zero delays. When `merge` is first pulled on, it pulls on both `ab` and `xy` concurrently.

## Termination

The resulting stream of `merge` terminates when the last input stream is done.

The operators `mergeHaltBoth`, `mergeHaltL` and `mergeHaltR` have different terminaton behaviours.

### Termination on either input with `mergeHaltBoth`

The resulting stream of `mergeHaltBoth` terminates when either input stream is done.

The following example combines `ab` and `xy` with `mergeHaltBoth`. Experiment with different delays for `ab`. Note that if `ab` is done first, no more elements are pulled from `xy` and the resulting stream terminates. Conversely, if `xy` is done first, no more elements are pulled from `ab`.

@:exampleWithInput(mergeHaltBoth) {
  drawChunked = false
}

Note that when `ab` emits all elements immediately (the seconds between `ab` elements is `0`), the `xy` stream is not run at all. The `ab` stream is done before the `xy` stream is pulled on.

### Termination on the left with `mergeHaltL`

The resulting stream of `mergeHaltL` terminates when the left input stream is done.

The following example combines `ab` and `xy` with `mergeHaltL`. Experiment with different delays for `ab`. Note that the resulting stream always terminates when `ab` is done.

@:exampleWithInput(mergeHaltL) {
  drawChunked = false
}

The last element of the stream is not guaranteed to be an output of the left stream. If an element is outputted by the right stream concurrently to the termination of the left stream, it may be outputted as the last element.

### Termination on the right with `mergeHaltR`

The resulting stream of `mergeHaltR` terminates when the right input stream is done.

The following example combines `ab` and `xy` with `mergeHaltR`. Experiment with different delays for `ab`. Note that the resulting stream always terminates when `xy` is done.


@:exampleWithInput(mergeHaltR) {
  drawChunked = false
}

As with `mergeHaltL`, the last element of the stream is not guaranteed to be an output of the right stream. If an element is outputted by the left stream concurrently to the termination of the right stream, it may be outputted as the last element.

## Exit cases on termination

### Exit cases on `mergeHalt` operators

An input stream that is terminated due to the completion of another input stream has an exit case of `Canceled`. 

 - When using `mergeHaltBoth`, one of the input streams may be canceled.
 - When using `mergeHaltL`, the right input stream may be canceled when the left input stream is done.
 - When using `mergeHaltR`, the left input stream may be canceled when the right input stream is done.
 
This can be observed with `onFinalizeCase`. 

The following example shows the exit cases of both input streams when merged with `mergeHaltBoth`.

Experiment with varying the delay of `ab`. Note that if the `ab` stream is done first, the `xy` stream has an exit case of `Canceled`. Conversely, if the `xy` stream is done, the `ab` stream has an exit case of `Canceled`.

@:exampleWithInput(mergeHaltBothExitCase) {
  drawChunked = false
}

### Exit cases on resulting stream termination

If a fixed number of elements are requested from the resulting stream, it may terminate before the input streams are done. The input streams are canceled.

The following example shows the exit cases when a single element is pulled from the resulting stream. Both `ab` and `xy` are canceled.

@:example(takeExitCase) {
  drawChunked = false
}

### Exit cases on error

If an error is raised in an input stream, the other input stream is terminated with an exit case of `Canceled`.

In this example, the `ab` stream raises an error when it is pulled on for the second time.

If the `xy` stream is stopped due to an error raised by the `ab` stream, its exit case is `Canceled`.

@:exampleWithInput(errorExitCase) {
  drawChunked = false
}

## Error propagation

Errors in the left or right input stream are propagated to the resulting stream.

In the following example, the `ab` stream raises an error when it is pulled on for the second time. The error is propagated to the resulting stream, causing the entire program to terminate with the error.

Experiment with the delay before the error.

@:exampleWithInput(mergeError) {
  drawChunked = false
}


## Resource management

The left or right streams may have finalizers associated with them. These finalizers are executed when their associated stream terminates.

In thid example, finalizers are associated with the `ab` and `xy` streams.

Experiment with different delays for `ab`. Note that the `ab` finalizer is executed when the `ab` stream is done, not when the resulting stream is done. Similarly, the `xy` finalizer is executed when the `xy` stream is done.

@:exampleWithInput(resources) {
  drawChunked = false
}

### Resource management on error

If an error is raised in either the left or right stream, the finalizers are run.

In this example, the `ab` stream raises an error when it is pulled for the second time. As before, finalizers are associated with the `ab` and `xy` streams.

Experiment with the delay before the error. Notice that the `ab` and `xy` finalizers are always run, provided that their respective streams are pulled on.

@:exampleWithInput(resourcesError) {
  drawChunked = false
}

Note that when `ab` raises an error immediately (the delay before the error is `0`), the `xy` finalizer is not run. The error is raised before the `xy` stream is pulled on.
