package edu.memphis.netlab.homesec;


import edu.memphis.netlab.homesec.security.BlockCipher;

public interface Constants {

  // ndn name constants
  String PROTOCOL = "homesec";
  String OWNER = "owner";
  String BOOTSTRAP = "bootstrap";
  String PUBKEY = "pubkey";
  String DEVICE = "device";

  String PREFIX_LOCAL_HOME = "/" + PROTOCOL;
  String PREFIX_OWNER = "/" + OWNER;
  String PREFIX_BOOTSTRAP = "/" + BOOTSTRAP;
  String PREFIX_DEVICE = "/" + DEVICE;
  String PREFIX_OWNER_BOOTSTRAP = PREFIX_LOCAL_HOME + PREFIX_BOOTSTRAP + PREFIX_OWNER;
  String PREFIX_DEVICE_BOOTSTRAP = PREFIX_LOCAL_HOME + PREFIX_BOOTSTRAP + PREFIX_DEVICE;
  String SUFFIX_DEVICE_BOOTSTRAP_PUBKEY = "/" + PUBKEY;

  // the first item after protocol prefix
  int NAME_COMPONENT_ROOT = 1;

  // Error messages
  String NOT_FOUND = "E: Not Found";
  String INVALID_STATE = "E: Invalid State";

  // ==============================================

  String DEFAULT_BT_DELIMITER = "|";

  // service intent keys
  String KEY_LOGIN_TOKEN = "token";
  String KEY_CMD_NAME = "cmd";
  String BROADCAST_ACTION_DEVICE_BT = "edu.memphis.netlab.homesec.BROADCAST.btr";
  String BROADCAST_ACTION_DEVICE_BT_SUC = "edu.memphis.netlab.homesec.BROADCAST.btr.success";
  String BROADCAST_ACTION_DEVICE_BT_FAIL = "edu.memphis.netlab.homesec.BROADCAST.btr.failed";
  String BROADCAST_ACTION_DEVICE_BT_MSG = "edu.memphis.netlab.homesec.BROADCAST.btr.msg";
  String BOOTSTRAP_DEVICE_ID = "edu.memphis.netlab.homesec.BOOTSTRAP.device.id";
  String BOOTSTRAP_DEVICE_DESCRIPTION = "edu.memphis.netlab.homesec.BOOTSTRAP.device.desc";

  // Bootstrap Session Manager
  int SESSION_TIMEOUT_MS = 5000;
  int DEFAULT_PUBLIC_KEY_SIZE = 256;
  int KEY_STRENGTH = BlockCipher.KEY_SIZE_128;
  int NAME_COM_BT_D_ID = 5;
  int INTEREST_LIFE_MS = 2000;
  int DATA_FRESHNESS_PERIOD = INTEREST_LIFE_MS;
  int MAX_RETRY = 5;
  long DEFAULT_RETRY_INTERVAL_MS = 200;

  // fixed core thread pool size
  int THREAD_POOL_SIZE = 8;

  // ==============================================

}
