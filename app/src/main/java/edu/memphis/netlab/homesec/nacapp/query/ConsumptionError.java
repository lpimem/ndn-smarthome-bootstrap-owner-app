package edu.memphis.netlab.homesec.nacapp.query;

/**
 * Description:
 * <p>
 * Author: lei
 */

public class ConsumptionError extends NodeHelper.ProcessError {
  public ConsumptionError() {
  }

  public ConsumptionError(String message) {
    super(message);
  }

  public ConsumptionError(Throwable e) {
    super(e);
  }

  public ConsumptionError(String message, Throwable e) {
    super(message, e);
  }
}
