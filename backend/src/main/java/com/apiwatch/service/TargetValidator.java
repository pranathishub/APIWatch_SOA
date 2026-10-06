package com.apiwatch.service;

import com.apiwatch.exception.ApiException;
import java.net.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TargetValidator {

  private final boolean allowPrivate;

  public TargetValidator(
    @Value("${apiwatch.allow-private-targets:false}") boolean allowPrivate
  ) {
    this.allowPrivate = allowPrivate;
  }

  public URI validate(String url) {
    try {
      URI u = URI.create(url);
      if (
        !java.util.Set.of("http", "https").contains(u.getScheme()) ||
        u.getHost() == null ||
        u.getUserInfo() != null ||
        u.getFragment() != null ||
        u.getPort() > 65535
      ) throw new Exception();
      return u;
    } catch (Exception e) {
      throw new ApiException(
        400,
        "Enter a valid HTTP or HTTPS URL without credentials or fragments"
      );
    }
  }

  public URI checkDestination(String url) throws UnknownHostException {
    URI u = validate(url);
    if (!allowPrivate) {
      for (InetAddress a : InetAddress.getAllByName(u.getHost())) {
        byte[] b = a.getAddress();
        boolean privateV6 = b.length == 16 && (b[0] & 254) == 252;
        if (
          a.isAnyLocalAddress() ||
          a.isLoopbackAddress() ||
          a.isLinkLocalAddress() ||
          a.isSiteLocalAddress() ||
          a.isMulticastAddress() ||
          privateV6
        ) throw new ApiException(400, "Private network targets are disabled");
      }
    }
    return u;
  }
}
