package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.certificate.Certificate;

import edu.memphis.netlab.homesec.security.Identity;

/**
 * Description:
 * <p>
 * Date: 5/19/17
 * Author: lei
 */

public class ServiceInfo extends Identity {

  public ServiceInfo(Identity provider, Name n, Certificate c, String description) {
    super(n, c);
    m_provider = provider;
  }

  @Override
  public int hashCode(){
    return this.Name().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ServiceInfo that = (ServiceInfo) o;
    return Name().equals(that.Name()) && isValid() == that.isValid();

  }

  public String Description(){
    return m_desc;
  }

  public Identity Provider(){
    return m_provider;
  }

  private Identity m_provider;
  private String m_desc;
}