package ru.spbau.mit.Serialization;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializerImpl<T> implements Serializer<T> {

    private static final Logger logger = Logger.getLogger(Serializer.class.getName());

    @Override
    public void serialize(T obj, OutputStream out) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(out);
        output.writeObject(obj);
        output.close();
    }

    @Override
    public T deserialize(InputStream in) throws IOException {
        ObjectInputStream input = new ObjectInputStream(in);
        T retVal = null;
        try {
            retVal = (T) input.readObject();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Class not found during serialization");
        }
        input.close();
        return retVal;
    }
}
