# -*- coding: utf-8 -*-
import sqlite3
import time
import web

def post(self):
    try:
        web.header('content-type','text/html;charset=utf-8',unique=True) 
        #获取参数
        nowTime = long(time.time()*1000)
        config = self.settings['config']
        ss = self.get_argument("img","")
        phone = self.get_argument("phone","")
        ajax = self.get_argument("ajax","")
        other = self.get_argument("other","")
        cx = sqlite3.connect(config.main_path+"/get/webcam.db")
        cu = cx.cursor()
        if other != "" and ss == "":
            cu.execute("select other from webcam where phone='%s'"%(phone))
            sss = cu.fetchall()
            if sss == None or len(sss) == 0:
                cu.close()
                cx.close()
                return ""
            else:
                for sss1 in sss:
                    if sss1[0] == None:
                        other = ""
                    else:
                        other = sss1[0]
                cu.close()
                cx.close()
                return other
        if ss != "":
            cu.execute("select * from webcam where phone='%s'"%(phone))
            sss = cu.fetchall()
            if sss == None or len(sss) == 0:
                cu.execute("insert into webcam(time,img,phone,other) values(%s,'%s','%s','%s')" %(str(nowTime),ss,phone,other))  
                cx.commit()
                cu.close()
                cx.close()
                return
            else:
                cu.execute("update webcam set time=%s,img='%s',other='%s' where phone='%s'" %(str(nowTime),ss,other,phone))
                cx.commit()
                cu.close()
                cx.close()
                return
        else:
            cu.execute("select * from webcam where phone='%s'"%(phone))
            sss = cu.fetchall()
            if sss == None or len(sss) == 0:
                cu.close()
                cx.close()
                return
            else:
                cu.execute("update webcam set other='20' where phone='%s'" %(phone))
                cx.commit()
                if ajax == "":
                    img = "data:image/png;base64,"
                    for sss1 in sss:
                        img = img + sss1[2]
                        break
                    cu.close()
                    cx.close()
                    html = """
                    <img id="ids" src="%s" />
                    <script type="text/javascript">
                    var xmlhttp;
                    if (window.XMLHttpRequest)
                      {// code for IE7+, Firefox, Chrome, Opera, Safari
                      xmlhttp=new XMLHttpRequest();
                      }
                    else
                      {// code for IE6, IE5
                      xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
                      }
                    window.setInterval(function(){ 
                        xmlhttp.open("GET","/webcam?phone=%s&ajax=ajax",false);
                        xmlhttp.send();
                        document.getElementById("ids").src=xmlhttp.responseText;
                    }, 100); 
                    </script>
                    """%(img,phone)
                    return html
                else:
                    img = "data:image/png;base64,"
                    for sss1 in sss:
                        img = img + sss1[2]
                        break
                    cu.close()
                    cx.close()
                    return img
    except Exception,e:
        print e
        return u'{"code":-1,"tip":"未知错误"}' + e