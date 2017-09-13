package edu.memphis.netlab.homesec.security;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.certificate.Certificate;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 * <p> An identity is a name associaited with a certificate </p>
 * Date: 5/19/17
 * Author: lei
 */

public class Identity {

  private static AtomicLong idcounter = new AtomicLong();

  public Identity(Name n, Certificate c) {
    this.m_name = n;
    this.m_cert = c;
    this.m_id = idcounter.addAndGet(1);
  }

  @Override
  public String toString() {
    return String.format(Locale.ENGLISH, "%s [%f]", m_name.toUri(), m_cert.getNotAfter());
  }

  public long Id() {
    return m_id;
  }

  public Name Name() {
    return this.m_name;
  }

  public Certificate Certificate() {
    return this.m_cert;
  }

  public void updateCertificate(Certificate cert) {
    this.m_cert = cert;
  }

  public boolean isValid() {
    return !this.m_cert.isTooEarly() && !this.m_cert.isTooLate();
  }

  private Name m_name;
  private Certificate m_cert;
  private long m_id;
}
