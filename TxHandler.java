import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

    UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        double sumIn = 0;
        double sumOut = 0;

        ArrayList<UTXO> lsUTXO = this.utxoPool.getAllUTXO();
        int cntUTXO[] = new int[lsUTXO.size()];

        // (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
        for (int i=0;i<inputs.size();i++) {
            Transaction.Input inp = inputs.get(i);
            boolean flag = false;
            for (int j=0;j<lsUTXO.size();j++) {
                UTXO utxo = lsUTXO.get(j);
                if (Arrays.equals(utxo.getTxHash(),inp.prevTxHash) && utxo.getIndex()==inp.outputIndex) {
                    flag = true;
                    sumIn += utxoPool.getTxOutput(utxo).value;

                    // (2) the signatures on each input of {@code tx} are valid,
                    PublicKey pubKey = utxoPool.getTxOutput(utxo).address;
                    byte[] message = tx.getRawDataToSign(i);
                    byte[] signature = inp.signature;
                    if (!(Crypto.verifySignature(pubKey, message, signature))) {
                        return false;
                    }
                    
                    // (3) no UTXO is claimed multiple times by {@code tx},
                    if (cntUTXO[j]>0) {
                        return false;
                    }
                    cntUTXO[j]+=1;
                }
            }
            if (flag==false) {
                return false;
            }
        }

        for (Transaction.Output o:outputs) {
            sumOut += o.value;
            // (4) all of {@code tx}s output values are non-negative,
            if (o.value<0) {
                return false;
            }
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        if (sumIn<sumOut) {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> ret = new ArrayList<>();
        for (int i=0;i<possibleTxs.length;i++){
            if (isValidTx(possibleTxs[i])){
                ret.add(possibleTxs[i]);

                // Remove input from UTXO Pool
                ArrayList<Transaction.Input> inputs = possibleTxs[i].getInputs();
                for (int j=0;j<inputs.size();j++){
                    Transaction.Input inp = inputs.get(j);
                    ArrayList<UTXO> lsUTXO = this.utxoPool.getAllUTXO();
                    for (int k=0;k<lsUTXO.size();k++){
                        UTXO utxo = lsUTXO.get(k);
                        if (Arrays.equals(utxo.getTxHash(),inp.prevTxHash) && utxo.getIndex()==inp.outputIndex) {
                            this.utxoPool.removeUTXO(utxo);
                            break;
                        }
                    }
                }
                
                // Add output to UTXO Pool
                ArrayList<Transaction.Output> outputs = possibleTxs[i].getOutputs();
                for (int j=0;j<outputs.size();j++) {
                    UTXO utxo = new UTXO(possibleTxs[i].getHash(),j);
                    utxoPool.addUTXO(utxo, possibleTxs[i].getOutput(j));
                }
            }
        }

        Transaction[] result = new Transaction[ret.size()];
        for (int i=0;i<ret.size();i++){
            result[i]=ret.get(i);
        }
        return result;
    }

}
