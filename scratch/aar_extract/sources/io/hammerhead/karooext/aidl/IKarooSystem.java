/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package io.hammerhead.karooext.aidl;
public interface IKarooSystem extends android.os.IInterface
{
  /** Default implementation for IKarooSystem. */
  public static class Default implements io.hammerhead.karooext.aidl.IKarooSystem
  {
    @Override public java.lang.String libVersion() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.os.Bundle info() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void dispatchEffect(android.os.Bundle bundle) throws android.os.RemoteException
    {
    }
    @Override public void addEventConsumer(java.lang.String id, android.os.Bundle bundle, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
    {
    }
    @Override public void removeEventConsumer(java.lang.String id) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements io.hammerhead.karooext.aidl.IKarooSystem
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an io.hammerhead.karooext.aidl.IKarooSystem interface,
     * generating a proxy if needed.
     */
    public static io.hammerhead.karooext.aidl.IKarooSystem asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof io.hammerhead.karooext.aidl.IKarooSystem))) {
        return ((io.hammerhead.karooext.aidl.IKarooSystem)iin);
      }
      return new io.hammerhead.karooext.aidl.IKarooSystem.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_libVersion:
        {
          java.lang.String _result = this.libVersion();
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_info:
        {
          android.os.Bundle _result = this.info();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_dispatchEffect:
        {
          android.os.Bundle _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          this.dispatchEffect(_arg0);
          break;
        }
        case TRANSACTION_addEventConsumer:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.os.Bundle _arg1;
          _arg1 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          io.hammerhead.karooext.aidl.IHandler _arg2;
          _arg2 = io.hammerhead.karooext.aidl.IHandler.Stub.asInterface(data.readStrongBinder());
          this.addEventConsumer(_arg0, _arg1, _arg2);
          break;
        }
        case TRANSACTION_removeEventConsumer:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.removeEventConsumer(_arg0);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements io.hammerhead.karooext.aidl.IKarooSystem
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public java.lang.String libVersion() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_libVersion, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public android.os.Bundle info() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_info, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void dispatchEffect(android.os.Bundle bundle) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, bundle, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_dispatchEffect, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void addEventConsumer(java.lang.String id, android.os.Bundle bundle, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          _Parcel.writeTypedObject(_data, bundle, 0);
          _data.writeStrongInterface(handler);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addEventConsumer, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void removeEventConsumer(java.lang.String id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_removeEventConsumer, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_libVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_info = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_dispatchEffect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_addEventConsumer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_removeEventConsumer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
  }
  public static final java.lang.String DESCRIPTOR = "io.hammerhead.karooext.aidl.IKarooSystem";
  public java.lang.String libVersion() throws android.os.RemoteException;
  public android.os.Bundle info() throws android.os.RemoteException;
  public void dispatchEffect(android.os.Bundle bundle) throws android.os.RemoteException;
  public void addEventConsumer(java.lang.String id, android.os.Bundle bundle, io.hammerhead.karooext.aidl.IHandler handler) throws android.os.RemoteException;
  public void removeEventConsumer(java.lang.String id) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
