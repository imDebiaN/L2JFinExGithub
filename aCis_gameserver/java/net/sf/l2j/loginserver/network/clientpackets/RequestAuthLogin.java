package net.sf.l2j.loginserver.network.clientpackets;

import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import org.slf4j.Logger;

import javax.crypto.Cipher;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.LoginController.AuthLoginResult;
import net.sf.l2j.loginserver.model.AccountInfo;
import net.sf.l2j.loginserver.model.GameServerInfo;
import net.sf.l2j.loginserver.network.LoginClient;
import net.sf.l2j.loginserver.network.LoginClient.LoginClientState;
import net.sf.l2j.loginserver.network.SessionKey;
import net.sf.l2j.loginserver.network.serverpackets.AccountKicked;
import net.sf.l2j.loginserver.network.serverpackets.AccountKicked.AccountKickedReason;
import net.sf.l2j.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.loginserver.network.serverpackets.LoginOk;
import net.sf.l2j.loginserver.network.serverpackets.ServerList;

public class RequestAuthLogin extends L2LoginClientPacket {

	private static Logger _log = LoggerFactory.getLogger(RequestAuthLogin.class.getName());

	private final byte[] _raw = new byte[128];

	private String _user;
	private String _password;
	private int _ncotp;

	public String getPassword() {
		return _password;
	}

	public String getUser() {
		return _user;
	}

	public int getOneTimePassword() {
		return _ncotp;
	}

	@Override
	public boolean readImpl() {
		if (super._buf.remaining() >= 128) {
			readB(_raw);
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		byte[] decrypted = null;
		final LoginClient client = getClient();
		try {
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		} catch (GeneralSecurityException e) {
			_log.info("", e);
			return;
		}

		try {
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		} catch (Exception e) {
			_log.warn("", e);
			return;
		}

		final InetAddress clientAddr = client.getConnection().getInetAddress();

		final AccountInfo info = LoginController.getInstance().retrieveAccountInfo(clientAddr, _user, _password);
		if (info == null) {
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}

		final AuthLoginResult result = LoginController.getInstance().tryCheckinAccount(client, clientAddr, info);
		switch (result) {
			case AUTH_SUCCESS:
				client.setAccount(info.getLogin());
				client.setState(LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt()));
				client.sendPacket((Config.SHOW_LICENCE) ? new LoginOk(client.getSessionKey()) : new ServerList(client));
				break;

			case INVALID_PASSWORD:
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;

			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
				break;

			case ALREADY_ON_LS:
				final LoginClient oldClient = LoginController.getInstance().getAuthedClient(info.getLogin());
				if (oldClient != null) {
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					LoginController.getInstance().removeAuthedLoginClient(info.getLogin());
				}
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;

			case ALREADY_ON_GS:
				final GameServerInfo gsi = LoginController.getInstance().getAccountOnGameServer(info.getLogin());
				if (gsi != null) {
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);

					if (gsi.isAuthed()) {
						gsi.getGameServerThread().kickPlayer(info.getLogin());
					}
				}
				break;
		}
	}
}
