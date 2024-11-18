package ch.heigvd.dai;

public class Errno {
  public static final int EPERM = 1; // Operation not permitted
  public static final int ENOENT = 2; // No such file or directory
  public static final int ESRCH = 3; // No such process
  public static final int EINTR = 4; // Interrupted system call
  public static final int EIO = 5; // Input/output error
  public static final int ENXIO = 6; // No such device or address
  public static final int E2BIG = 7; // Argument list too long
  public static final int ENOEXEC = 8; // Exec format error
  public static final int EBADF = 9; // Bad file descriptor
  public static final int ECHILD = 10; // No child processes
  public static final int EAGAIN = 11; // Resource temporarily unavailable
  public static final int ENOMEM = 12; // Cannot allocate memory
  public static final int EACCES = 13; // Permission denied
  public static final int EFAULT = 14; // Bad address
  public static final int ENOTBLK = 15; // Block device required
  public static final int EBUSY = 16; // Device or resource busy
  public static final int EEXIST = 17; // File exists
  public static final int EXDEV = 18; // Invalid cross-device link
  public static final int ENODEV = 19; // No such device
  public static final int ENOTDIR = 20; // Not a directory
  public static final int EISDIR = 21; // Is a directory
  public static final int EINVAL = 22; // Invalid argument
  public static final int ENFILE = 23; // Too many open files in system
  public static final int EMFILE = 24; // Too many open files
  public static final int ENOTTY = 25; // Inappropriate ioctl for device
  public static final int ETXTBSY = 26; // Text file busy
  public static final int EFBIG = 27; // File too large
  public static final int ENOSPC = 28; // No space left on device
  public static final int ESPIPE = 29; // Illegal seek
  public static final int EROFS = 30; // Read-only file system
  public static final int EMLINK = 31; // Too many links
  public static final int EPIPE = 32; // Broken pipe
  public static final int EDOM = 33; // Numerical argument out of domain
  public static final int ERANGE = 34; // Numerical result out of range
  public static final int EDEADLK = 35; // Resource deadlock avoided
  public static final int ENAMETOOLONG = 36; // File name too long
  public static final int ENOLCK = 37; // No locks available
  public static final int ENOSYS = 38; // Function not implemented
  public static final int ENOTEMPTY = 39; // Directory not empty
  public static final int ELOOP = 40; // Too many levels of symbolic links
  public static final int ENOMSG = 42; // No message of desired type
  public static final int EIDRM = 43; // Identifier removed
  public static final int ECHRNG = 44; // Channel number out of range
  public static final int EL2NSYNC = 45; // Level 2 not synchronized
  public static final int EL3HLT = 46; // Level 3 halted
  public static final int EL3RST = 47; // Level 3 reset
  public static final int ELNRNG = 48; // Link number out of range
  public static final int EUNATCH = 49; // Protocol driver not attached
  public static final int ENOCSI = 50; // No CSI structure available
  public static final int EL2HLT = 51; // Level 2 halted
  public static final int EBADE = 52; // Invalid exchange
  public static final int EBADR = 53; // Invalid request descriptor
  public static final int EXFULL = 54; // Exchange full
  public static final int ENOANO = 55; // No anode
  public static final int EBADRQC = 56; // Invalid request code
  public static final int EBADSLT = 57; // Invalid slot
  public static final int EDEADLOCK = 35; // Resource deadlock avoided
  public static final int EBFONT = 59; // Bad font file format
  public static final int ENOSTR = 60; // Device not a stream
  public static final int ENODATA = 61; // No data available
  public static final int ETIME = 62; // Timer expired
  public static final int ENOSR = 63; // Out of streams resources
  public static final int ENONET = 64; // Machine is not on the network
  public static final int ENOPKG = 65; // Package not installed
  public static final int EREMOTE = 66; // Object is remote
  public static final int ENOLINK = 67; // Link has been severed
  public static final int EADV = 68; // Advertise error
  public static final int ESRMNT = 69; // Srmount error
  public static final int ECOMM = 70; // Communication error on send
  public static final int EPROTO = 71; // Protocol error
  public static final int EMULTIHOP = 72; // Multihop attempted
  public static final int EDOTDOT = 73; // RFS specific error
  public static final int EBADMSG = 74; // Bad message
  public static final int EOVERFLOW = 75; // Value too large for defined data type
  public static final int ENOTUNIQ = 76; // Name not unique on network
  public static final int EBADFD = 77; // File descriptor in bad state
  public static final int EREMCHG = 78; // Remote address changed
  public static final int ELIBACC = 79; // Cannot access a needed shared library
  public static final int ELIBBAD = 80; // Accessing a corrupted shared library
  public static final int ELIBSCN = 81; // .lib section in a.out corrupted
  public static final int ELIBMAX = 82; // Attempting to link in too many shared libraries
  public static final int ELIBEXEC = 83; // Cannot exec a shared library directly
  public static final int EILSEQ = 84; // Invalid or incomplete multibyte or wide character
  public static final int ERESTART = 85; // Interrupted system call should be restarted
  public static final int ESTRPIPE = 86; // Streams pipe error
  public static final int EUSERS = 87; // Too many users
  public static final int ENOTSOCK = 88; // Socket operation on non-socket
  public static final int EDESTADDRREQ = 89; // Destination address required
  public static final int EMSGSIZE = 90; // Message too long
  public static final int EPROTOTYPE = 91; // Protocol wrong type for socket
  public static final int ENOPROTOOPT = 92; // Protocol not available
  public static final int EPROTONOSUPPORT = 93; // Protocol not supported
  public static final int ESOCKTNOSUPPORT = 94; // Socket type not supported
  public static final int EOPNOTSUPP = 95; // Operation not supported
  public static final int EPFNOSUPPORT = 96; // Protocol family not supported
  public static final int EAFNOSUPPORT = 97; // Address family not supported by protocol
  public static final int EADDRINUSE = 98; // Address already in use
  public static final int EADDRNOTAVAIL = 99; // Cannot assign requested address
  public static final int ENETDOWN = 100; // Network is down
  public static final int ENETUNREACH = 101; // Network is unreachable
  public static final int ENETRESET = 102; // Network dropped connection on reset
  public static final int ECONNABORTED = 103; // Software caused connection abort
  public static final int ECONNRESET = 104; // Connection reset by peer
  public static final int ENOBUFS = 105; // No buffer space available
  public static final int EISCONN = 106; // Transport endpoint is already connected
  public static final int ENOTCONN = 107; // Transport endpoint is not connected
  public static final int ESHUTDOWN = 108; // Cannot send after transport endpoint shutdown
  public static final int ETOOMANYREFS = 109; // Too many references: cannot splice
  public static final int ETIMEDOUT = 110; // Connection timed out
  public static final int ECONNREFUSED = 111; // Connection refused
  public static final int EHOSTDOWN = 112; // Host is down
  public static final int EHOSTUNREACH = 113; // No route to host
  public static final int EALREADY = 114; // Operation already in progress
  public static final int EINPROGRESS = 115; // Operation now in progress
  public static final int ESTALE = 116; // Stale file handle
  public static final int EUCLEAN = 117; // Structure needs cleaning
  public static final int ENOTNAM = 118; // Not a XENIX named type file
  public static final int ENAVAIL = 119; // No XENIX semaphores available
  public static final int EISNAM = 120; // Is a named type file
  public static final int EREMOTEIO = 121; // Remote I/O error
  public static final int EDQUOT = 122; // Disk quota exceeded
  public static final int ENOMEDIUM = 123; // No medium found
  public static final int EMEDIUMTYPE = 124; // Wrong medium type
  public static final int ECANCELED = 125; // Operation canceled
  public static final int ENOKEY = 126; // Required key not available
  public static final int EKEYEXPIRED = 127; // Key has expired
  public static final int EKEYREVOKED = 128; // Key has been revoked
  public static final int EKEYREJECTED = 129; // Key was rejected by service
  public static final int EOWNERDEAD = 130; // Owner died
  public static final int ENOTRECOVERABLE = 131; // State not recoverable
  public static final int ERFKILL = 132; // Operation not possible due to RF-kill
  public static final int EHWPOISON = 133; // Memory page has hardware error
  public static final int ENOTSUP = 95; // Operation not supported (alias for EOPNOTSUPP)

  // Constructor private to prevent instantiation
  private Errno() {
  }

  public static String getErrorMessage(int errorCode) {
    switch (errorCode) {
      case EPERM:
        return "Operation not permitted";
      case ENOENT:
        return "No such file or directory";
      case ESRCH:
        return "No such process";
      case EINTR:
        return "Interrupted system call";
      case EIO:
        return "Input/output error";
      case ENXIO:
        return "No such device or address";
      case E2BIG:
        return "Argument list too long";
      case ENOEXEC:
        return "Exec format error";
      case EBADF:
        return "Bad file descriptor";
      case ECHILD:
        return "No child processes";
      case EAGAIN:
        return "Resource temporarily unavailable";
      case ENOMEM:
        return "Cannot allocate memory";
      case EACCES:
        return "Permission denied";
      case EFAULT:
        return "Bad address";
      case ENOTBLK:
        return "Block device required";
      case EBUSY:
        return "Device or resource busy";
      case EEXIST:
        return "File exists";
      case EXDEV:
        return "Invalid cross-device link";
      case ENODEV:
        return "No such device";
      case ENOTDIR:
        return "Not a directory";
      case EISDIR:
        return "Is a directory";
      case EINVAL:
        return "Invalid argument";
      case ENFILE:
        return "Too many open files in system";
      case EMFILE:
        return "Too many open files";
      case ENOTTY:
        return "Inappropriate ioctl for device";
      case ETXTBSY:
        return "Text file busy";
      case EFBIG:
        return "File too large";
      case ENOSPC:
        return "No space left on device";
      case ESPIPE:
        return "Illegal seek";
      case EROFS:
        return "Read-only file system";
      case EMLINK:
        return "Too many links";
      case EPIPE:
        return "Broken pipe";
      case EDOM:
        return "Numerical argument out of domain";
      case ERANGE:
        return "Numerical result out of range";
      case EDEADLK:
        return "Resource deadlock avoided";
      case ENAMETOOLONG:
        return "File name too long";
      case ENOLCK:
        return "No locks available";
      case ENOSYS:
        return "Function not implemented";
      case ENOTEMPTY:
        return "Directory not empty";
      case ELOOP:
        return "Too many levels of symbolic links";
      case ENOMSG:
        return "No message of desired type";
      case EIDRM:
        return "Identifier removed";
      case ECHRNG:
        return "Channel number out of range";
      case EL2NSYNC:
        return "Level 2 not synchronized";
      case EL3HLT:
        return "Level 3 halted";
      case EL3RST:
        return "Level 3 reset";
      case ELNRNG:
        return "Link number out of range";
      case EUNATCH:
        return "Protocol driver not attached";
      case ENOCSI:
        return "No CSI structure available";
      case EL2HLT:
        return "Level 2 halted";
      case EBADE:
        return "Invalid exchange";
      case EBADR:
        return "Invalid request descriptor";
      case EXFULL:
        return "Exchange full";
      case ENOANO:
        return "No anode";
      case EBADRQC:
        return "Invalid request code";
      case EBADSLT:
        return "Invalid slot";
      case EBFONT:
        return "Bad font file format";
      case ENOSTR:
        return "Device not a stream";
      case ENODATA:
        return "No data available";
      case ETIME:
        return "Timer expired";
      case ENOSR:
        return "Out of streams resources";
      case ENONET:
        return "Machine is not on the network";
      case ENOPKG:
        return "Package not installed";
      case EREMOTE:
        return "Object is remote";
      case ENOLINK:
        return "Link has been severed";
      case EADV:
        return "Advertise error";
      case ESRMNT:
        return "Srmount error";
      case ECOMM:
        return "Communication error on send";
      case EPROTO:
        return "Protocol error";
      case EMULTIHOP:
        return "Multihop attempted";
      case EDOTDOT:
        return "RFS specific error";
      case EBADMSG:
        return "Bad message";
      case EOVERFLOW:
        return "Value too large for defined data type";
      case ENOTUNIQ:
        return "Name not unique on network";
      case EBADFD:
        return "File descriptor in bad state";
      case EREMCHG:
        return "Remote address changed";
      case ELIBACC:
        return "Cannot access a needed shared library";
      case ELIBBAD:
        return "Accessing a corrupted shared library";
      case ELIBSCN:
        return ".lib section in a.out corrupted";
      case ELIBMAX:
        return "Attempting to link in too many shared libraries";
      case ELIBEXEC:
        return "Cannot exec a shared library directly";
      case EILSEQ:
        return "Invalid or incomplete multibyte or wide character";
      case ERESTART:
        return "Interrupted system call should be restarted";
      case ESTRPIPE:
        return "Streams pipe error";
      case EUSERS:
        return "Too many users";
      case ENOTSOCK:
        return "Socket operation on non-socket";
      case EDESTADDRREQ:
        return "Destination address required";
      case EMSGSIZE:
        return "Message too long";
      case EPROTOTYPE:
        return "Protocol wrong type for socket";
      case ENOPROTOOPT:
        return "Protocol not available";
      case EPROTONOSUPPORT:
        return "Protocol not supported";
      case ESOCKTNOSUPPORT:
        return "Socket type not supported";
      case EOPNOTSUPP:
        return "Operation not supported";
      case EPFNOSUPPORT:
        return "Protocol family not supported";
      case EAFNOSUPPORT:
        return "Address family not supported by protocol";
      case EADDRINUSE:
        return "Address already in use";
      case EADDRNOTAVAIL:
        return "Cannot assign requested address";
      case ENETDOWN:
        return "Network is down";
      case ENETUNREACH:
        return "Network is unreachable";
      case ENETRESET:
        return "Network dropped connection on reset";
      case ECONNABORTED:
        return "Software caused connection abort";
      case ECONNRESET:
        return "Connection reset by peer";
      case ENOBUFS:
        return "No buffer space available";
      case EISCONN:
        return "Transport endpoint is already connected";
      case ENOTCONN:
        return "Transport endpoint is not connected";
      case ESHUTDOWN:
        return "Cannot send after transport endpoint shutdown";
      case ETOOMANYREFS:
        return "Too many references: cannot splice";
      case ETIMEDOUT:
        return "Connection timed out";
      case ECONNREFUSED:
        return "Connection refused";
      case EHOSTDOWN:
        return "Host is down";
      case EHOSTUNREACH:
        return "No route to host";
      case EALREADY:
        return "Operation already in progress";
      case EINPROGRESS:
        return "Operation now in progress";
      case ESTALE:
        return "Stale file handle";
      case EUCLEAN:
        return "Structure needs cleaning";
      case ENOTNAM:
        return "Not a XENIX named type file";
      case ENAVAIL:
        return "No XENIX semaphores available";
      case EISNAM:
        return "Is a named type file";
      case EREMOTEIO:
        return "Remote I/O error";
      default:
        return "Unknown error";
    }
  }

}
