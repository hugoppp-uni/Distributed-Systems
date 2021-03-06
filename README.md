#  

**About arc42**

arc42, the Template for documentation of software and system
architecture.

Created and maintained by Dr. Peter Hruschka, Dr. Gernot Starke and
contributors.

Template Revision: 8.0 EN (based on asciidoc), February 2022

© We acknowledge that this document uses material from the arc 42
architecture template, <https://arc42.org>.

# Introduction and Goals

This document describes the distributed control system for at least 16 robotic arms for the distributed systems 
practical exam part.

## Requirements Overview

![Use case diagram](diagrams/ucd.png)

| ID  | Requirement    | Explanation                                                                                                        |
|-----|----------------|--------------------------------------------------------------------------------------------------------------------|
| UC0 | move_arm       | change the position of the arm in three axis: left/right, back/forth, up/down. Absolute values should be provided. |
| UC1 | toggle_gripper | toggle the gripper state between open and closed                                                                   |


## Quality Goals

## Stakeholders

| Name            | Role                   | Contact                        | Expectations                                          |
|-----------------|------------------------|--------------------------------|-------------------------------------------------------|
| Martin Becke    | Customer               | martin.becke@haw-hamburg.de    | Working control-system in accordance to documentation |
| Hugo Protsch    | Developer / Maintainer | hugo.protsch@haw-hamburg.de    |                                                       |
| Justin Hoffmann | Developer / Maintainer | justin.hoffmann@haw-hamburg.de |                                                       |

# Architecture Constraints

The control-system shall:

- be integrable with any robotic arm that implements the ICaDSRoboticArm Interface
- be scalable for at least 16 robotic arms 
- contain a custom IDL and source generator
- contain a custom nameserver

# System Scope and Context

## Business Context

**\<Diagram or Table>**

**\<optionally: Explanation of external domain interfaces>**

## Technical Context

**\<Diagram or Table>**

**\<optionally: Explanation of technical interfaces>**

**\<Mapping Input/Output to Channels>**

# Solution Strategy

# Building Block View

## Whitebox Overall System

![Component Diagram](diagrams/cmp_level0.png)
![Component Diagram](diagrams/cmp_level1.png)

Motivation

:   *\<text explanation>*

Contained Building Blocks

:   *\<Description of contained building block (black boxes)>*

Important Interfaces

:   *\<Description of important interfaces>*

### \<Name black box 1>

*\<Purpose/Responsibility>*

*\<Interface(s)>*

*\<(Optional) Quality/Performance Characteristics>*

*\<(Optional) Directory/File Location>*

*\<(Optional) Fulfilled Requirements>*

*\<(optional) Open Issues/Problems/Risks>*

### \<Name black box 2>

*\<black box template>*

### \<Name black box n>

*\<black box template>*

### \<Name interface 1>

...

### \<Name interface m>

## Level 2

### White Box *\<building block 1>*

*\<white box template>*

### White Box *\<building block 2>*

*\<white box template>*

...

### White Box *\<building block m>*

*\<white box template>*

## Level 3

### White Box \<\_building block x.1\_\>

*\<white box template>*

### White Box \<\_building block x.2\_\>

*\<white box template>*

### White Box \<\_building block y.1\_\>

*\<white box template>*

# Runtime View

## \<Runtime Scenario 1>

- *\<insert runtime diagram or textual description of the scenario>*

- *\<insert description of the notable aspects of the interactions
  between the building block instances depicted in this diagram.\>*

## \<Runtime Scenario 2>

## ...

## \<Runtime Scenario n>

# Deployment View

## Infrastructure Level 1

***\<Overview Diagram>***

Motivation

:   *\<explanation in text form>*

Quality and/or Performance Features

:   *\<explanation in text form>*

Mapping of Building Blocks to Infrastructure

:   *\<description of the mapping>*

## Infrastructure Level 2

### *\<Infrastructure Element 1>*

*\<diagram + explanation>*

### *\<Infrastructure Element 2>*

*\<diagram + explanation>*

...

### *\<Infrastructure Element n>*

*\<diagram + explanation>*

# Cross-cutting Concepts

## *\<Concept 1>*

*\<explanation>*

## *\<Concept 2>*

*\<explanation>*

...

## *\<Concept n>*

*\<explanation>*

# Architecture Decisions

# Quality Requirements

## Quality Tree

## Quality Scenarios

# Risks and Technical Debts

# Glossary

+-----------------------+-----------------------------------------------+
| Term | Definition |
+=======================+===============================================+
| *\<Term-1>*           | *\<definition-1>*                             |
+-----------------------+-----------------------------------------------+
| *\<Term-2>*           | *\<definition-2>*                             |
+-----------------------+-----------------------------------------------+
