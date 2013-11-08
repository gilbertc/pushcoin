package com.pushcoin.core.exceptions;

import com.pushcoin.core.net.Server;

public class ServerException extends RecoverableException {

	private static final long serialVersionUID = 7832094285116135786L;
	private Server.ErrorCode ec;

	public Server.ErrorCode getErrorCode() {
		return ec;
	}

	public ServerException(Server.ErrorCode ec) {
		super("Failed to contact PushCoin server");
		this.ec = ec;
	}
}