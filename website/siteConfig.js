/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

/* List of projects/orgs using your project for the users page */
const users = [];

const siteConfig = {
  title: "Battery Metrics" /* title for your website */,
  tagline: "Instrument system metrics quickly and cheaply",
  url: "https://github.com/facebookincubator/Battery-Metrics" /* your github url */,
  baseUrl: "/battery-metrics/" /* base url for your project */,
  projectName: "Battery-Metrics",
  headerLinks: [
    { doc: "quickstart", label: "QuickStart" },
    { doc: "api", label: "API" },
    { page: "help", label: "Help" },
    { blog: false, label: "Blog" }
  ],
  users,
  /* path to images for header/footer */
  headerIcon: "img/battery-metrics.svg",
  footerIcon: "img/battery-metrics.svg",
  favicon: "img/favicon.png",
  /* colors for website */
  colors: {
    primaryColor: "#2E8555",
    secondaryColor: "#205C3B",
    prismColor:
      "rgba(46, 133, 85, 0.03)" /* primaryColor in rgba form, with 0.03 alpha */
  },
  // This copyright info is used in /core/Footer.js and blog rss/atom feeds.
  copyright: "Copyright © " + new Date().getFullYear() + " Facebook"
};

module.exports = siteConfig;
