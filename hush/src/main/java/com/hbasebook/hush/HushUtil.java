package com.hbasebook.hush;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HushUtil {
  /**
   * The digits used to BASE encode the short Ids.
   */
  private static final String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  public static String hushEncode(long number) {
    return longToString(number, baseDigits.length(), true);
  }

  public static long hushDecode(String id) {
    return parseLong(id, baseDigits.length(), true);
  }

  /**
   * Encodes a number in BASE N.
   * 
   * @param number The number to encode.
   * @param base The base to use for the encoding.
   * @param reverse Flag to indicate if the result should be reversed.
   * @return The encoded - and optionally reversed - encoded string.
   */
  private static String longToString(long number, int base, boolean reverse) {
    String result = number == 0 ? "0" : "";
    while (number != 0) {
      int mod = (int) number % base;
      if (reverse) {
        result += baseDigits.charAt(mod);
      } else {
        result = baseDigits.charAt(mod) + result;
      }
      number = number / base;
    }
    return result;
  }

  /**
   * Decodes the given BASE N encoded value.
   * 
   * @param number The encoded value to decode.
   * @param base The base to decode with.
   * @param reverse Flag to indicate how the encoding was done.
   * @return The decoded number.
   */
  private static long parseLong(String number, int base, boolean reverse) {
    int index = number.length();
    int result = 0;
    int multiplier = 1;
    while (index-- > 0) {
      int pos = reverse ? number.length() - (index + 1) : index;
      result += baseDigits.indexOf(number.charAt(pos)) * multiplier;
      multiplier = multiplier * base;
    }
    return result;
  }

  public static String fixNull(String s) {
    if (s == null) {
      return "";
    }
    return s;
  }

  public static String getOrSetUsername(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    Principal principal = request.getUserPrincipal();
    String username = null;
    if (principal != null) {
      username = principal.getName();
    }
    if (username == null) {
      // no principal found
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals("auid")) {
          username = cookie.getValue();
        }
      }
    }
    if (username == null) {
      // no principal and no cookie found in request
      // check response first, maybe an enclosing jsp set it
      username = (String) request.getAttribute("auid");
    }
    if (username == null) {
      // we really don't have one,
      // let's create a new cookie
      username = ResourceManager.getInstance().getUserManager().generateAnonymousUserId();
      response.addCookie(new Cookie("auid", username));
      // add as a request attribute so chained servlets can get to it
      request.setAttribute("auid", username);
    }
    return username;
  }

}
