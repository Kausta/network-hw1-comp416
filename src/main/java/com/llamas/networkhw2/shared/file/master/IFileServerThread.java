package com.llamas.networkhw2.shared.file.master;

import com.llamas.networkhw2.shared.file.follower.IFileClient;

/**
 * Every file server thread is a IFileServerThread, which is a thread that implements IFileClient functionality
 */
public abstract class IFileServerThread extends Thread implements IFileClient {
}
