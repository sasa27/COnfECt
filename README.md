# COnfECt
This is the first implementation of the COnfECt method, used for preliminary tests.

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

[ ] put the correlation coefficient and similarity coefficient as arguments
