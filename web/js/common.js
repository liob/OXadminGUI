String.prototype.trim = function () {
    return this.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");
}

function Number(input) {
    if(input.value.length > 0) {
        var clean = "";
        var bad = 0;
        for(var  i = 0; i < input.value.length; ++i) {
            var tmp = input.value.charAt(i);
            if(!( tmp >= '0' && tmp <= '9')) {
                bad = bad + 1;
                continue;
            }
            clean = clean + tmp;
        }
        if(bad > 0) {
            input.value = clean;

        }
    }
}

function ShowStackTrace() {
    var url = document.location.href.substr(0,document.location.href.indexOf("index.html")) + "stacktrace.html";
    var w = window.open(url, "Stacktrace", "width=300,height=200,scrollbars=yes,resizable=yes");
    w.focus();
}


/*
  Copyright Lutz Eymers <ixtab@polzin.com>, 1997
  Polzin GmbH, Duesseldorf
*/
<!--
function isDigit( ch )
{
    if ( (ch >= '0') && (ch <= '9') )
        return true;
    else
        return false;
}


function isAlpha( ch )
{
    if ( ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')) )
        return true;
    else
        return false;
}


function isAlnum( ch )
{
    if ( isAlpha( ch ) || isDigit( ch ) )
        return true;
    else
        return false;
}


function notIn( str1, str2 )
{
    var i = 0;
    var j = str2.length;
    for( ; i<j; i++ )
    {
        var str3 =  str2.charAt(i);
        if( str1.indexOf( str3 ) != -1 )
            return false;
    }
    return true;
}


function checkUsername( username, mustBeQuoted )
{
    var i = 0;
    var j = username.length;
    if ( username.charAt(0) != '"' )
    {
        if ( (username.charAt(0) <  ' ') || (username.charAt(0) >  '~')
            || !notIn( mustBeQuoted, username.charAt(0) ) )
            return false;
        for( i=1; i<j; i++ )
        {
            if ( ( (username.charAt(i) < ' ') || (username.charAt(i) >  '~')
                || !notIn ( mustBeQuoted, username.charAt(i) ) )
            && ( username.charAt(i-1) != '\\' ) )
                return false;
        }
    }
    else
    {
        if ( username.charAt( j-1 ) != '"' )
            return false;
        for( i=1; i<j-1; i++ )
        {
            if ( ( (username.charAt(i) == '\n') || (username.charAt(i) == '\r')
                || (username.charAt(i) == '\"') )
            && (username.charAt(i-1) != '\\') )
                return false;
        }

    }
    return true;
}


function checkNr ( nr )
{
    var i=0;
    var j=nr.length;

    if( j < 1 )
        return false;

    for( ; i<j; i++ )
        if( ( nr.charAt(i) < '0' ) || ( nr.charAt(i) > '9' ) )
            return false;

    return true;
}


function checkIpnr( ipnr )
{
    var iL=0;
    var iC=0;
    var i=0;
    var sNr = "";

    for( ; i< ipnr.length; i++ )
    {
        if ( ipnr.charAt(i) == '.' )
        {
            if ( !iL || (iL> 3) || parseInt( sNr,10 ) > 255 )
                return false;
            iC++;
            iL = 0;
            sNr = "";
            continue;
        }
        if ( isDigit ( ipnr.charAt(i) ) )
        {
            iL++;
            sNr = sNr + ipnr.charAt(i);
            continue;
        }
        return false;
    }

    if ( parseInt( sNr,10 ) > 255 )
        return false;
    if ( ( (iC==3) && (iL>=1) && (iL<=3) ) || ( (iC==4) && (!iL) )  )
        return true;
    else
        return false;
}


function checkFqdn( fqdn )
{
    var iL=0;
    var iC=0;
    var i=fqdn.length-1;

    if ( (fqdn.charAt(0) == '.') || (fqdn.charAt(0) == '-') )
        return false;
    if ( fqdn.charAt(i) == '.' )
        i=i-1;

    for( ; i>=0; i-- )
    {
        if ( fqdn.charAt(i) == '.' )
        {
            if ( iL < 2 && iC < 2 )
                return false;
            if ( fqdn.charAt(i-1) == '-' )
                return false;
            iC++;
            iL = 0;
            continue;
        }
        if ( isAlnum ( fqdn.charAt(i) ) )
        {
            iL++;
            continue;
        }
        if ( fqdn.charAt(i) == '-' )
        {
            if ( !iL )
                return false;
            iL++;
            continue;
        }
        return false;
    }

    if ( !iC || ( iL == 1 && iC < 2 ) || ( !iL && iC==1 ) ) {
        return false;
    }

    return true;

}


function checkHostname( hostname )
{
    if ( hostname.charAt(0) == '[' )
    {
        if ( hostname.charAt(hostname.length-1) != ']' )
            return false;
        var ipnr = hostname.substring( 1, hostname.length -1 );
        return checkIpnr( ipnr );
    }

    if ( hostname.charAt(0) == '#' )
    {
        var nr = hostname.substring( 1, hostname.length );
        return checkNr( nr );
    }

    return checkFqdn( hostname );
}


function checkEmailAdr( address )
{
    var status = true;
    var username = "";
    var hostname = "";

    if ( address.length < 8 )
        return false;

    var seperate = address.lastIndexOf("@");
    if ( seperate == -1 )
        return false;

    username = address.substring(0, seperate );
    if ( ! checkUsername( username, "<>()[],;:@\" " ) )
        return false;

    hostname = address.substring(seperate+1, address.length );
    if ( ! checkHostname( hostname ) )
        return false;

    return true;
}


function checkEmail( email, allowFullname )
{
    var existFullname = false;
    var status = true;
    var fullname = "";
    var adress = "";
    if ( email.length < 8 )
        return false;
    var emailBegin = email.indexOf("<");
    var emailEnd = email.lastIndexOf(">");

    if ( (emailBegin == -1) && (emailEnd == -1) )
        return checkEmailAdr( email );

    if ( ( (emailBegin == -1) && (emailEnd != -1) )
        || ( (emailBegin != -1) && (emailEnd == -1) ) )
        return false;

    adress = email.substring( emailBegin+1, emailEnd );

    if ( ! checkEmailAdr( adress ) )
        return false;

    if ( email.length == adress.length + 2 )
        return true;
    else
    if ( ! allowFullname )
        return false;

    if ( emailEnd == email.length - 1 )
    {
        if ( emailBegin == 0 )
            return true;
        if ( email.charAt( emailBegin -1 ) != ' ' )
            return false;
        fullname = email.substring( 0, emailBegin-1 );
        return checkUsername ( fullname, "<>()[],;:@\"" );
    }

    return false ;

}


