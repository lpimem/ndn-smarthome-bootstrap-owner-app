package edu.memphis.netlab.homesec.nacapp.query;

import java.io.Serializable;
import java.util.List;

/**
 * Description:
 * <p>
 * Author: lei
 */

public class ServiceInfoList implements Serializable {

  public List<ServiceInfo> getServices() {
    return services;
  }

  // following JSON naming convention
  private List<ServiceInfo> services;
}
