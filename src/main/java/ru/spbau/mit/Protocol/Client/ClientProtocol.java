package ru.spbau.mit.Protocol.Client;

import ru.spbau.mit.Protocol.RemoteFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Perform all the client protocol requests from here:
 * http://hwproj.me/tasks/5785
 */
public interface ClientProtocol {
    void sendListRequest(DataOutputStream output) throws IOException;

    List<RemoteFile> readListResponse(DataInputStream input) throws IOException;

    void sendUploadRequest(DataOutputStream output, String name, long size) throws IOException;

    int readUploadResponse(DataInputStream input) throws IOException;

    void sendSourcesRequest(DataOutputStream output, int fileId) throws IOException;

    List<InetSocketAddress> readSourcesResponse(DataInputStream input) throws IOException;

    void sendUpdateRequest(DataOutputStream output, short port, List<Integer> seedingFileIds) throws IOException;

    boolean readUpdateResponse(DataInputStream input) throws IOException;

    void sendStatRequest(DataOutputStream output, int fileId) throws IOException;

    List<Integer> readStatResponse(DataInputStream input) throws IOException;

    void sendGetRequest(DataOutputStream output, int fileId, int part) throws IOException;

    void readGetResponse(DataInputStream input, byte[] buffer) throws IOException;

}
