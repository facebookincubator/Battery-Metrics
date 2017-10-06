/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

/* List of projects/orgs using your project for the users page */
const users = [
  {
    caption: "User1",
    image: "/battery-metrics/img/docusaurus.svg",
    infoLink: "https://www.example.com",
    pinned: true
  }
];

const siteConfig = {
  title: "Battery Metrics" /* title for your website */,
  tagline: "A library that helps in instrumenting battery related system metrics.",
  url: "https://facebookincubator.github.io" /* your github url */,
  baseUrl: "/battery-metrics/" /* base url for your project */,
  projectName: "Battery-Metrics",
  headerLinks: [
    { doc: "doc1", label: "Docs" },
    { doc: "doc4", label: "API" },
    { page: "help", label: "Help" },
    { blog: true, label: "Blog" }
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
  copyright:
    "Copyright © " +
    new Date().getFullYear() +
    " Your Name or Your Company Name"
};

module.exports = siteConfig;
