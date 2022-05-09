# Scrooge Coin

## Introduction
An implementation of a crypto coin which called Scrooge Coin. This coin bases on UTXO model which can detect double spending problem. The assignment requires students to implement the `TxHandler.java` source to check the validity of a transaction with inputs and outputs (`TxHandler::isValidTx`); and, moreover, the handler for many transactions (`TxHandler::handleTxs`). 

## Models

### UTXO model
An `unspent transaction output` (shorten as UTXO) is a model allow users to track the ownership of currency. It has a hash pointer which points to a original transaction, and a index to determine which output of that transaction. To verify the equality between two UTXOs, they compare the hash bytes and index between them.  

```Java
public class UTXO implements Comparable<UTXO> {
  private byte[] txHash;
  private int index;
}
```

### UTXO Pool
An `UTXO pool` is a pool contains coins as a hash map which associates a `UTXO` which its output `Transaction::Output`. This model will keep track all the **unspent** coin to solve the double spending problem.  

```Java
public class UTXOPool {
  private HashMap<UTXO, Transaction.Output> H;
  
  public void removeUTXO(UTXO utxo);
  public void addUTXO(UTXO utxo, Transaction.Output txOut);
  public ArrayList<UTXO> getAllUTXO();
}
```

### Transaction
In this model, a transaction **consumes inputs** and **produces outputs**. 
Each input has:
- Hash pointer that points to the original transaction
- The index of output from that transaction (these two are the same as `UTXO`)
- And a signature to show the ownership of that coin
Indeed, you can remove an input with an associated UTXO.
```Java
public class Input {
  public byte[] prevTxHash;
  public int outputIndex;
  public byte[] signature;
  public void removeInput(UTXO ut);
}
```

Each output has:
- The value of spending coin
- The receipient address (public key)
```Java
public class Output {
  public double value;
  public PublicKey address;
}
```

In general, a transaction contains inputs, outputs and their hashed data. The transaction will be hashed (only hash inputs and outputs, except its previous hashed value) after each modification.
```Java
public class Transaction {
  private byte[] hash;
  private ArrayList<Input> inputs;
  private ArrayList<Output> outputs;

  public byte[] getRawTx();
  public void finalize();
}
```

### TxHandler
The `TxHandler` contains a `pool of UTXO` to do the verification for each transaction. In each transaction, we must guarantee:
- Each input of the transaction must associate with a output that exist in the UTXO pool
- The signature of input is valid (by verifying with the public from its corresponding output)
- No UTXO claim multiple times (double spending)
- All the outputs of transpace must be non-negative
- The total output values must less or equal to the inputs
Furthermore, an implementation for handle each epoch of transactions is required
```Java
public class TxHandler {
  UTXOPool utxoPool;

  public boolean isValidTx(Transaction tx);
  public Transaction[] handleTxs(Transaction[] possibleTxs);
}
```

## Reference
This is the assignment 1 from the course [Bitcoin and Cryptocurrency Technologies](https://www.coursera.org/learn/cryptocurrency) from Princeton University on Coursera. To maintain the course's rule, please **do not copy** without permission.
