# -*- coding: utf-8 -*-
"""
服务器程序主入口
"""
#引头
import sys
import imp
import os
import config  
import web

#更变运行字符环境
reload(sys)  
sys.setdefaultencoding('utf-8')  # @UndefinedVariable

urls = (
    '/(.*)', 'Index'
)

class DiySelf():
    def __init__(self,the_conf,the_argument):
        self.settings = {}
        self.settings["config"] = the_conf
        self.argument = the_argument
    
    def get_argument(self,name,df=None):
        return self.argument.get(name,df)
    
    def get_cookie(self,name):
        return web.cookies().get(name)
    
    def set_cookie(self,name,val,path="/"):
        web.setcookie(name, val,path=path)

class Index:    
    
    def GET(self,val):
        try:
            #获取url
            method = val
            method = method.replace("/","")
            mm = imp.load_source(method,config.main_path+"/get/"+method+".py")
            mtd = getattr(mm,"post")
            diy_self = DiySelf(config,web.input())
            return mtd(diy_self)
        except:
            return u'{"code":-2,"tip":"未知url"}'
        
if __name__ == "__main__":
    config.main_path = os.path.split(os.path.realpath(__file__))[0]
    web.config.debug = False
    app = web.application(urls, globals())
    app.run()

    
    
    
