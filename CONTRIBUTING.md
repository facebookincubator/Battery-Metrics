# Contributing to Battery Metrics

## Philosophy
The core idea behind the library is to keep it
1. Small: with minimal dependencies and not too many functions for those libraries maintaining a single dex.
2. Modular: customers should be able to use as much -- or as little -- as they like, even if it means more build files for the library authors.
3. ThreadSafe: Metrics Collectors should be completely thread safe.
4. Minimal resource impact: Collectors should be cheap to query and should try to minimize memory allocated. If there's no choice but to have something be expensive, then document that explictly.
5. Well tested: structure the code to be easily testable, and make sure all the tricky bits are covered.

## Pull Requests
We actively welcome your pull requests.

1. Fork the repo and create your branch from `master`.
2. If you've added code that should be tested, add tests.
3. If you've changed APIs, update the documentation.
4. Ensure the test suite passes.
5. Make sure your code lints.
6. If you haven't already, complete the Contributor License Agreement ("CLA").

## Contributor License Agreement ("CLA")
In order to accept your pull request, we need you to submit a CLA. You only need
to do this once to work on any of Facebook's open source projects.

Complete your CLA here: <https://code.facebook.com/cla>

## Issues
We use GitHub issues to track public bugs. Please ensure your description is
clear and has sufficient instructions to be able to reproduce the issue.

Facebook has a [bounty program](https://www.facebook.com/whitehat/) for the safe
disclosure of security bugs. In those cases, please go through the process
outlined on that page and do not file a public issue.

## Coding Style
* We use the [Google Java Style](https://github.com/google/google-java-format) to avoid thinking about formatting.
* Just run the formatter before uploading your patch.
* PS. There's an [IntelliJ Plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format) as well.

## License
By contributing to Battery Metrics, you agree that your contributions will be licensed
under the LICENSE file in the root directory of this source tree.
