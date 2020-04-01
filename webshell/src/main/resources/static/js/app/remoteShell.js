var first=true;
function connection() {
    if(first){
        $("#shell").append("正在初始化连接,请稍后.....\r");
        first=false;
    }
    var result;
    let cmd=$("#cmd").val()
    let cmddata={"cmd":cmd}
    $.ajax({
        type:"GET",
        url:"/v1/connection",
        data:cmddata,
        dataType:"json",
        success:function (data) {
            if(data.successList){
                let successList=data.successList;
                if(Array.isArray(successList)) {
                    for(let i=0;i<successList.length;i++) {
                        $("#shell").append(successList[i]);
                        //换行符
                        $("#shell").append("\r");
                    }
                    $("#shell").append("==================================================================================================\r");
                    $("#shell").scrollTop($("#shell")[0].scrollHeight );
                }
            }
            if(data.errorList){
                let errorList=data.errorList;
                if(Array.isArray(errorList)) {
                    for(let i=0;i<errorList.length;i++) {
                        $("#shell").append(errorList[i]);
                        //换行符
                        $("#shell").append("\r");
                    }
                    $("#shell").append("==================================================================================================\r");

                }
            }
            if(Object.keys(data).length === 0){
                $("#shell").append("无效或暂不支持命令!\r")
            }
        },
        error:function (data) {

        }
    });


}
function reconnection() {
    $.ajax({
        type: "GET",
        url: "/v1/reconnection",
        dataType: "text",
        success: function (data) {
            if(data=="success") {
                $("#shell").append("已重置!\r")
            }
        }
    });
}