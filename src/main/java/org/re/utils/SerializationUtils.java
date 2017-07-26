/**
 * 
 */
package org.re.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.re.model.Requirement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author doquocanh-macbook
 *
 */
public class SerializationUtils {

    public static boolean writeSerializedRequirements(String path, ObservableList<Requirement> requirements) {
        boolean success = false;
        try {
            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(new ArrayList<Requirement>(requirements));
            objOut.close();
            out.close();
            success = true;
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }
    
    @SuppressWarnings("unchecked")
    public static ObservableList<Requirement> readSerializedRequirements(String path) {
        ObservableList<Requirement> reqs = null;
        try {
            FileInputStream in = new FileInputStream(path);
            ObjectInputStream objIn = new ObjectInputStream(in);
            ArrayList<Requirement> reqArray = (ArrayList<Requirement>) objIn.readObject();
            reqs = FXCollections.observableArrayList(reqArray);
            objIn.close();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            reqs = null;
            e.printStackTrace();
        }
        return reqs;
    }
}
