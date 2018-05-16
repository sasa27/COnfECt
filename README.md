# COnfECt
This is the first implementation of the COnfECt method, used for preliminary tests.

## Method

COnfECt is a model inference method that infer LTSs (Labelled Transition Systems) that model each component of the system from execution traces. 
It is based KTails, a passive model learning method that work in two steps:

- First, it construct a tree from the traces, with each branch of the tree corresponding to a trace, and each event to an edge.
- Then, it merge all states of this tree that have the same k-futur, i.e. the nodes have the same futur of length k.

The method COnfECt add two steps to KTails: *Trace Analysis & Extraction*, and *LTS synchronisation*.

### Trace Analysis & Extraction

In this step, we try to see if two consecutive events in a trace are due to the same components, or if they are due to two different components and we have to extract one of the event.

For that we calculate a *Correlation Coeficient* for each pair of events, and we define a treshold above we consider that two components are due to the same component (we say that they have a strong correlation).
This *Correlation Coeficient* is used to separate each trace into many traces, containing events of only one component.

Each trace obtained with this step will produce a LTS with the first step of KTails.

### LTS Synchronisation

The goal of this step is to join LTSs that contain similar events, and model behaviours of the same component.

Three strategies are implemented:
- the **Strict** srategy, where we want limit the over-generalisation. we do not join LTSs, and we cannot repetitively call an other component.

- the **Weak** strategy, where we want to reduce the number of components. We join LTSs, and allow repetitive call of an other component.
- the **Strong** strategy, where we obtained a more general model. We joines LTSs, and each LTS can call any other LTS anytime.

Then the models are genralised with the second step of KTails, to obtain the final LTSs.

## Contents
**traces/** contains the different test cases (*Exp1-Exp7*, and test cases for the time execution).

**results/** contains the results of the different experiments.

**COnfECt/** contains the implementation of the COnfECt method.

## Usage

Put all your traces in the folder **COnfECt/traces**. If the folder **COnfECt/COnfECt** does not exist, make it. It have to be empty.

Go into the **COnfECt/** folder and execute the script **exec.sh** with the synchronisation strategy as arguments (```strict```, ```weak```, or ```strong```):

```
cd COnfECt
./exec.sh strict
```

Results are generated in the **COnfECt/COnfECt** folder.

## TODO

- [ ] put the correlation coefficient and similarity coefficient as arguments
